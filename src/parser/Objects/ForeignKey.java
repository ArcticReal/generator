package parser.Objects;

public class ForeignKey {
    private String name;
    private String relatedEntity;
    private String referencedField;
    private boolean many;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (relatedEntity != null ? !relatedEntity.equals(that.relatedEntity) : that.relatedEntity != null)
            return false;
        return referencedField != null ? referencedField.equals(that.referencedField) : that.referencedField == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (relatedEntity != null ? relatedEntity.hashCode() : 0);
        result = 31 * result + (referencedField != null ? referencedField.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ForeignKey{" +
                "name='" + name + '\'' +
                ", relatedEntity='" + relatedEntity + '\'' +
                ", referencedField='" + referencedField + '\'' +
                '}';
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getReferencedField() {
        return referencedField;
    }

    public void setReferencedField(String referencedField) {
        this.referencedField = referencedField;
    }
}
