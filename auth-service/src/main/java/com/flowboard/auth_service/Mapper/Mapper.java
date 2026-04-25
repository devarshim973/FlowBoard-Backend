package com.flowboard.auth_service.Mapper;

public interface Mapper<A, B>{
    B mapTo(A a);
    A mapFrom(B b);
}
