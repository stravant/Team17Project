package io.searchbox.core;

import com.google.gson.Gson;
import io.searchbox.action.GenericResultAbstractDocumentTargetedAction;

/**
 * @author Dogukan Sonmez
 * @author cihat keser
 */
public class MoreLikeThis extends GenericResultAbstractDocumentTargetedAction {

    private Object query;

    private MoreLikeThis(Builder builder) {
        super(builder);

        this.query = builder.query;
        setURI(buildURI());
    }

    @Override
    protected String buildURI() {
        StringBuilder sb = new StringBuilder(super.buildURI());
        sb.append("/_mlt");
        return sb.toString();
    }

    @Override
    public String getRestMethodName() {
        return (query != null) ? "POST" : "GET";
    }

    @Override
    public Object getData(Gson gson) {
        return query;
    }

    public static class Builder extends GenericResultAbstractDocumentTargetedAction.Builder<MoreLikeThis, Builder> {
        private Object query;

        public Builder(String index, String type, String id, Object query) {
            this.index(index);
            this.type(type);
            this.id(id);
            this.query = query;
        }

        public MoreLikeThis build() {
            return new MoreLikeThis(this);
        }

    }

}
