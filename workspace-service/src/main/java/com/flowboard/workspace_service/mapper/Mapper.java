package com.flowboard.workspace_service.mapper;

public interface Mapper<A, B>{
    B mapTo (A a);
    A mapFrom(B b);
}
