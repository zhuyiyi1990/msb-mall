package com.msb.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.mall.product.vo.Catalog2VO;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.CategoryDao;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());
        return new PageUtils(page);
    }

    /**
     * 查询所有的类别数据，然后将数据封装为树形结构，便于前端使用
     *
     * @param params
     * @return
     */
    @Override
    public List<CategoryEntity> queryPageWithTree(Map<String, Object> params) {
        // 1.查询所有的商品分类信息
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2.将商品分类信息拆解为树形结构【父子关系】
        // 第一步遍历出所有的大类  parent_cid = 0
        List<CategoryEntity> list = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0).map(categoryEntity -> {
                    // 根据大类找到多有的小类  递归的方式实现
                    categoryEntity.setChildren(getCategoryChildren(categoryEntity, categoryEntities));
                    return categoryEntity;
                }).sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        // 第二步根据大类找到对应的所有的小类
        return list;
    }

    /**
     * 逻辑批量删除操作
     *
     * @param ids
     */
    @Override
    public void removeCategoryByIds(List<Long> ids) {
        // TODO  1.检查类别数据是否在其他业务中使用
        // 2.批量逻辑删除操作
        baseMapper.deleteBatchIds(ids);
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catalogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
//    @CacheEvict(value = "category", key = "'getLevel1Category'")
//    @Caching(evict = {@CacheEvict(value = "category", key = "'getLevel1Category'"), @CacheEvict(value = "category", key = "'getCatalog2JSON'")})
    @CacheEvict(value = "category", allEntries = true)
    public void updateDetail(CategoryEntity entity) {
        // 更新类别名称
        this.updateById(entity);
        if (!StringUtils.isEmpty(entity.getName())) {
            // 同步更新级联的数据
            categoryBrandRelationService.updateCatalogName(entity.getCatId(), entity.getName());
            // TODO 同步更新其他的冗余数据
        }
    }

    /**
     * 查询出所有的商品大类(一级分类)
     * 在注解中我们可以指定对应的缓存的名称，起到一个分区的作用，一般按照业务来区分
     * Cacheable({"catagory","product"}) 代表当前的方法的返回结果是需要缓存的，
     * 调用该方法的时候，如果缓存中有数据，那么该方法就不会执行，
     * 如果缓存中没有数据，那么就执行该方法并且把查询的结果缓存起来
     * 缓存处理
     * 1.存储在Redis中的缓存数据的Key是默认生成的：缓存名称::SimpleKey[]
     * 2.默认缓存的数据的过期时间是-1永久
     * 3.缓存的数据，默认使用的是jdk的序列化机制
     * 改进：
     * 1.生成的缓存数据我们需要指定自定义的key： key属性来指定，可以直接字符串定义也可以通过SPEL表达式处理：#root.method.name
     * 2.指定缓存数据的存活时间: spring.cache.redis.time-to-live 指定过期时间
     * 3.把缓存的数据保存为JSON数据
     * SpringCache的原理
     * CacheAutoConfiguration--》根据指定的spring.cache.type=redis会导入 RedisCacheAutoConfiguration
     *
     * @return
     */
    @Trace
    //@Cacheable(value = {"category", "product"}, key = "'level1Category'")
    @Cacheable(value = "category", key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Category() {
        System.out.println("查询了数据库操作.....");
        long start = System.currentTimeMillis();
        List<CategoryEntity> list = baseMapper.queryLevel1Category();
        System.out.println("查询消耗的时间:" + (System.currentTimeMillis() - start));
        return list;
    }

    /**
     * 跟进父编号获取对应的子菜单信息
     *
     * @param list
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> queryByParentCid(List<CategoryEntity> list, Long parentCid) {
        List<CategoryEntity> collect = list.stream().filter(item -> {
            return item.getParentCid().equals(parentCid);
        }).collect(Collectors.toList());
        return collect;
    }

//    private Map<String, Map<String, List<Catalog2VO>>> cache = new HashMap<>();

    @Override
    @Cacheable(value = "category", key = "#root.methodName")
    public Map<String, List<Catalog2VO>> getCatalog2JSON() {
        // 获取所有的分类数据
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<>());
        // 获取所有的一级分类的数据
        List<CategoryEntity> level1Category = this.queryByParentCid(list, 0L);
        // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            // 根据一级分类的编号，查询出对应的二级分类的数据
            List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
            List<Catalog2VO> catalog2VOs = null;
            if (l2Catalogs != null) {
                catalog2VOs = l2Catalogs.stream().map(l2 -> {
                    // 需要把查询出来的二级分类的数据填充到对应的Catalog2VO中
                    Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 根据二级分类的数据找到对应的三级分类的信息
                    List<CategoryEntity> l3Catalogs = this.queryByParentCid(list, l2.getCatId());
                    if (l3Catalogs != null) {
                        // 获取到的二级分类对应的三级分类的数据
                        List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catalogs.stream().map(l3 -> {
                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        // 三级分类关联二级分类
                        catalog2VO.setCatalog3List(catalog3VOS);
                    }
                    return catalog2VO;
                }).collect(Collectors.toList());
            }
            return catalog2VOs;
        }));
        return map;
    }

    /**
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     *
     * @return
     */
    //@Override
    public Map<String, List<Catalog2VO>> getCatalog2JSONRedis() {
        String key = "catalogJSON";
        String catalogJSON = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存没有命中......");
            Map<String, List<Catalog2VO>> catalog2JSONForDb = getCatalog2JSONDbWithRedisson();
            /*if (catalog2JSONForDb == null) {
                stringRedisTemplate.opsForValue().set(key, "1", 5, TimeUnit.SECONDS);
            } else {
                String json = JSON.toJSONString(catalog2JSONForDb);
                stringRedisTemplate.opsForValue().set(key, json, 10, TimeUnit.MINUTES);
            }*/
            return catalog2JSONForDb;
        }
        System.out.println("缓存命中了........");
        Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
        });
        return stringListMap;
    }

    public Map<String, List<Catalog2VO>> getCatalog2JSONDbWithRedisson() {
        String key = "catalogJSON";
        RLock lock = redissonClient.getLock("catalog2JSON-lock");
        Map<String, List<Catalog2VO>> data = null;
        try {
            lock.lock();
            //加锁成功
            data = getDataForDB(key);
        } finally {
            lock.unlock();
        }
        return data;
    }

    public Map<String, List<Catalog2VO>> getCatalog2JSONDbWithRedisLock() {
        String keys = "catalogJSON";
        //加锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功......");
            Map<String, List<Catalog2VO>> data = null;
            try {
                //给对应的key设置过期时间
                //stringRedisTemplate.expire("lock", 20, TimeUnit.SECONDS);
                //加锁成功
                data = getDataForDB(keys);
            } finally {
                /*String val = stringRedisTemplate.opsForValue().get("lock");
                if (uuid.equals(val)) {
                    //释放锁
                    stringRedisTemplate.delete("lock");
                }*/
                String scripts = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<>(scripts, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        } else {
            //加锁失败
            //休眠+重试
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("获取锁失败......");
            return getCatalog2JSONDbWithRedisLock();
        }
    }

    private Map<String, List<Catalog2VO>> getDataForDB(String keys) {
        /*if (cache.containsKey("getCatalog2JSON")) {
            return cache.get("getCatalog2JSON");
        }*/
        System.out.println("------加锁-----》");
        String catalogJSON = stringRedisTemplate.opsForValue().get(keys);
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
            return stringListMap;
        }
        System.out.println("======>查询数据库操作");
        // 获取所有的分类数据
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<>());
        // 获取所有的一级分类的数据
        List<CategoryEntity> level1Category = this.queryByParentCid(list, 0L);
        // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
        Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            // 根据一级分类的编号，查询出对应的二级分类的数据
            List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
            List<Catalog2VO> catalog2VOs = null;
            if (l2Catalogs != null) {
                catalog2VOs = l2Catalogs.stream().map(l2 -> {
                    // 需要把查询出来的二级分类的数据填充到对应的Catalog2VO中
                    Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 根据二级分类的数据找到对应的三级分类的信息
                    List<CategoryEntity> l3Catalogs = this.queryByParentCid(list, l2.getCatId());
                    if (l3Catalogs != null) {
                        // 获取到的二级分类对应的三级分类的数据
                        List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catalogs.stream().map(l3 -> {
                            Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3VO;
                        }).collect(Collectors.toList());
                        // 三级分类关联二级分类
                        catalog2VO.setCatalog3List(catalog3VOS);
                    }
                    return catalog2VO;
                }).collect(Collectors.toList());
            }
            return catalog2VOs;
        }));
//            cache.put("getCatalog2JSON", map);
        if (map == null) {
            stringRedisTemplate.opsForValue().set(keys, "1", 5, TimeUnit.SECONDS);
        } else {
            String json = JSON.toJSONString(map);
            stringRedisTemplate.opsForValue().set(keys, json, 10, TimeUnit.MINUTES);
        }
        return map;
    }

    /**
     * 从数据库查询的结果
     * 查询出所有的二级和三级分类的数据
     * 并封装为Map<String, Catalog2VO>对象
     *
     * @return
     */
    public Map<String, List<Catalog2VO>> getCatalog2JSONForDb() {
        String keys = "catalogJSON";
        synchronized (this) {
            /*if (cache.containsKey("getCatalog2JSON")) {
                return cache.get("getCatalog2JSON");
            }*/
            System.out.println("------加锁-----》");
            String catalogJSON = stringRedisTemplate.opsForValue().get(keys);
            if (!StringUtils.isEmpty(catalogJSON)) {
                Map<String, List<Catalog2VO>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2VO>>>() {
                });
                return stringListMap;
            }
            System.out.println("======>查询数据库操作");
            // 获取所有的分类数据
            List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<>());
            // 获取所有的一级分类的数据
            List<CategoryEntity> level1Category = this.queryByParentCid(list, 0L);
            // 把一级分类的数据转换为Map容器 key就是一级分类的编号， value就是一级分类对应的二级分类的数据
            Map<String, List<Catalog2VO>> map = level1Category.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
                // 根据一级分类的编号，查询出对应的二级分类的数据
                List<CategoryEntity> l2Catalogs = this.queryByParentCid(list, value.getCatId());
                List<Catalog2VO> catalog2VOs = null;
                if (l2Catalogs != null) {
                    catalog2VOs = l2Catalogs.stream().map(l2 -> {
                        // 需要把查询出来的二级分类的数据填充到对应的Catalog2VO中
                        Catalog2VO catalog2VO = new Catalog2VO(l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                        // 根据二级分类的数据找到对应的三级分类的信息
                        List<CategoryEntity> l3Catalogs = this.queryByParentCid(list, l2.getCatId());
                        if (l3Catalogs != null) {
                            // 获取到的二级分类对应的三级分类的数据
                            List<Catalog2VO.Catalog3VO> catalog3VOS = l3Catalogs.stream().map(l3 -> {
                                Catalog2VO.Catalog3VO catalog3VO = new Catalog2VO.Catalog3VO(l3.getParentCid().toString(), l3.getCatId().toString(), l3.getName());
                                return catalog3VO;
                            }).collect(Collectors.toList());
                            // 三级分类关联二级分类
                            catalog2VO.setCatalog3List(catalog3VOS);
                        }
                        return catalog2VO;
                    }).collect(Collectors.toList());
                }
                return catalog2VOs;
            }));
//            cache.put("getCatalog2JSON", map);
            if (map == null) {
                stringRedisTemplate.opsForValue().set(keys, "1", 5, TimeUnit.SECONDS);
            } else {
                String json = JSON.toJSONString(map);
                stringRedisTemplate.opsForValue().set(keys, json, 10, TimeUnit.MINUTES);
            }
            return map;
        }
    }

    /**
     * 225,22,2
     *
     * @param catalogId
     * @param paths
     * @return
     */
    private List<Long> findParentPath(Long catalogId, List<Long> paths) {
        paths.add(catalogId);
        CategoryEntity entity = this.getById(catalogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 查找该大类下的所有的小类  递归查找
     *
     * @param categoryEntity   某个大类
     * @param categoryEntities 所有的类别数据
     * @return
     */
    private List<CategoryEntity> getCategoryChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(entity -> {
                    // 根据大类找到他的直属的小类
                    // 注意 Long 数据比较 不在 -128 127之间的数据是 new Long() 对象
                    return entity.getParentCid().equals(categoryEntity.getCatId());
                }).map(entity -> {
                    // 根据这个小类递归找到对应的小小类
                    entity.setChildren(getCategoryChildren(entity, categoryEntities));
                    return entity;
                }).sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return collect;
    }

}