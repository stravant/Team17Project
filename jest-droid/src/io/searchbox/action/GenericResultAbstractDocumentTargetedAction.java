package io.searchbox.action;

import com.google.gson.Gson;
import io.searchbox.client.JestResult;

/**
 * @author cihat keser
 */
public abstract class GenericResultAbstractDocumentTargetedAction extends AbstractDocumentTargetedAction<JestResult> {

    public GenericResultAbstractDocumentTargetedAction(Builder builder) {
        super(builder);
    }

    @Override
    public JestResult createNewElasticSearchResult(String json, int statusCode, String reasonPhrase, Gson gson) {
        return createNewElasticSearchResult(new JestResult(gson), json, statusCode, reasonPhrase, gson);
    }

}
