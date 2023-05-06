package com.imokkkk.serializable.kryo;

import com.esotericsoftware.kryo.Kryo;

/**
 * Kryo线程不安全 使用Kryo Pool或ThreadLocal
 *
 * @author liuwy
 * @date 2023-05-06 13:16
 * @since 1.0
 */
public class KryoFactory {
    private final ThreadLocal<Kryo> holder =
            new ThreadLocal<Kryo>() {
                @Override
                protected Kryo initialValue() {
                    return createKryo();
                }

                private Kryo createKryo() {
                    Kryo kryo = new Kryo();
                    kryo.setReferences(true); // 支持循环引用
                    kryo.setRegistrationRequired(false); // 关闭注册行为
                    return kryo;
                }
            };

    public Kryo getKryo() {
        return holder.get();
    }
}
