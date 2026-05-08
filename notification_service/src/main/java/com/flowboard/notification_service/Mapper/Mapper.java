package com.flowboard.notification_service.Mapper;

public interface Mapper<A, B>{
    B mapTo (A a);
    A mapFrom(B b);
}
