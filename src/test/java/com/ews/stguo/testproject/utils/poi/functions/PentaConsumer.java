package com.ews.stguo.testproject.utils.poi.functions;

/**
 * @author <a href="mailto:v-stguo@expedia.com">steven</a>
 */
public interface PentaConsumer<R, I, M, E, S> {

    void accept(R r, I i, M m, E e, S s);

}
