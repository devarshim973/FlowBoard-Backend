package com.flowboard.card_service.mapper;

public interface Mapper<A, B>{
    B mapTo (A a);
    A mapFrom(B b);
}
