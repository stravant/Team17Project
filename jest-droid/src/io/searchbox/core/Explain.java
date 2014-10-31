package io.searchbox.core;

import com.google.gson.Gson;
import io.searchbox.action.GenericResultAbstractDocumentTargetedAction;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class Explain extends GenericResultAbstractDocumentTargetedAction {

    private Object query;

    private Explain(Builder builder) {
        super(builder);
        setURI(buildURI());
        this.query = builder.query;
    }

    @Override
    public String getRestMethodName() {
        return "GET";
    }

    @Override
    public Object getData(Gson gson) {
        return query;
    }

    @Override
    protected String buildURI() {
        StringBuilder sb = new StringBuilder(super.buildURI());
        sb.append("/_explain");
        return sb.toString();
    }

    public static class Builder extends GenericResultAbstractDocumentTargetedAction.Builder<Explain, Builder> {
        private final Object query;

        public Builder(String index, String type, String id, Object query) {
            this.index(index);
            this.type(type);
            this.id(id);
            this.query = query;
        }

        public Explain build() {
            return new Explain(this);
        }

    }

}
