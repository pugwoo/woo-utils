package com.pugwoo.wooutils.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestJsonSerializer {

    public static class BigDecimalDTO {

        @JsonSerialize(using = BigDecimal2ScaleSerializer.class)
        private BigDecimal a;

        public BigDecimal getA() {
            return a;
        }

        public void setA(BigDecimal a) {
            this.a = a;
        }
    }

    @Test
    public void test() {
        BigDecimalDTO dto = new BigDecimalDTO();
        dto.setA(new BigDecimal("1.2345"));

        String json = JSON.toJson(dto);
        System.out.println(json);
        assert json.equals("{\"a\":1.23}");
    }

}
