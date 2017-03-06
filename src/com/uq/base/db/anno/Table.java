package com.uq.base.db.anno;
import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 @Retention(RetentionPolicy.RUNTIME)
 @Target(value=ElementType.TYPE)
public @interface Table {
	public String value();//声明一个属性
}
