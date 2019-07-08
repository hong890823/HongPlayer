package com.hongplayer.base;


import com.hongplayer.util.BeanUtil;

import java.io.Serializable;

public class BaseBean implements Serializable {

    public static final long serialVersionUID = -316172390920775219L;

    @Override
    public String toString() {
        return BeanUtil.bean2string(this);
    }

}
