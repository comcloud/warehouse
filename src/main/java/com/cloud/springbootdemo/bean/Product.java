package com.cloud.springbootdemo.bean;

import com.cloud.springbootdemo.util.Lists;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * @version v1.0
 * @ClassName Product
 * @Author rayss
 * @Datetime 2024/7/27 6:36 PM
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Product implements Iterable<String> {


    /**
     * 产品名
     */
    private String name;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 数量
     */
    private String quantity;

    /**
     * 单价
     */
    private String unitPrice;

    /**
     * 金额，即等于数量*单价
     */
    public String getAmount() {
        //没有单价，总额记为0
        return StringUtils.isNotEmpty(unitPrice) ? String.valueOf(String.format("%.2f", Double.parseDouble(quantity) * Double.parseDouble(unitPrice))) : "0";

    }

    /**
     * 返回可用属性数量，不包括id，算上了amount
     */
    public int getAttributeNum() {
        return 5;
    }


    public Product(Product product) {
        this.name = product.getName();
        this.unit = product.getUnit();
        this.quantity = product.getQuantity();
        this.unitPrice = product.getUnitPrice();
    }

    /**
     * 规定，产品名和产品单位相同时才认为是同一个产品，因为同样的产品可能使用不同的单位，这时候认为他们是不同的Product
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        return new EqualsBuilder().append(name, product.name).append(unit, product.unit).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(unit).toHashCode();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<String> iterator() {
        return new Itr();
    }


    private class Itr implements Iterator<String> {

        private final List<String> attributeSeq = Lists.newArraryList(name, unit, quantity, unitPrice, getAmount());

        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;


        public boolean hasNext() {
            return cursor != getAttributeNum();
        }

        public String next() {
            try {
                int i = cursor;
                String next = attributeSeq.get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }


    }
}
