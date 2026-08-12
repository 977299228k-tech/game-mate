package com.gamemate.common;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("未授权或登录已过期");
    }
}
