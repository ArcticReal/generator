package parser.Objects;

import java.io.Serializable;
import java.util.List;

public class EntityTxtJson implements Serializable{

    private String entityName;
    private List<EntityField> fields;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityTxtJson that = (EntityTxtJson) o;

        if (entityName != null ? !entityName.equals(that.entityName) : that.entityName != null) return false;
        return fields != null ? fields.equals(that.fields) : that.fields == null;
    }

    @Override
    public int hashCode() {
        int result = entityName != null ? entityName.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntityTxtJson{" +
                "entityName='" + entityName + '\'' +
                ", fields=" + fields +
                '}';
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<EntityField> getFields() {
        return fields;
    }

    public void setFields(List<EntityField> fields) {
        this.fields = fields;
    }
}
