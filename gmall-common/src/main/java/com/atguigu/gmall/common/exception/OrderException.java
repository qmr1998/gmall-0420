package com.atguigu.gmall.common.exception;

/**
 * @author Lee
 * @date 2020-10-19  19:26
 */
public class OrderException extends RuntimeException {

    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
