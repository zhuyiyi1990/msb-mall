package com.msb.mall.order.web;

import com.msb.common.exception.NoStockException;
import com.msb.mall.order.service.OrderService;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        // TODO 查询订单确认页需要的信息
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("confirmVo", confirmVo);
        return "confirm";
    }

    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitVO vo, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("vo = " + vo);
        Integer code = 0;
        OrderResponseVO responseVO = null;
        try {
            responseVO = orderService.submitOrder(vo);
            code = responseVO.getCode();
        } catch (NoStockException e) {
            code = 2;
        }
        if (code == 0) {
            model.addAttribute("orderResponseVO", responseVO);
            // 表示下单操作成功
            return "pay";
        } else {
            System.out.println("code = " + code);
            String msg = "订单失败";
            if (code == 1) {
                msg = msg + ":重复提交";
            } else if (code == 2) {
                msg = msg + ":锁定库存失败";
            }
            //redirectAttributes.addAttribute("msg", msg);
            redirectAttributes.addFlashAttribute("msg", msg);
            // 表示下单操作失败
            return "redirect:http://order.msb.com/toTrade";
        }
    }

    @PostMapping("/orderPay")
    public String orderPay(@RequestParam("orderSn") String orderSn) {
        // TODO 完成相关的支付操作
        System.out.println("orderSn = " + orderSn);
        return "list";
    }

}