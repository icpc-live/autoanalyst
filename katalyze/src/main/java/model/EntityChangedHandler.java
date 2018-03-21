package model;

import io.EntityOperation;

public interface EntityChangedHandler {
    void entityChanged(ApiEntity entity, EntityOperation op);
}
