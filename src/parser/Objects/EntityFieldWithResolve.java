package parser.Objects;

import java.util.List;

public class EntityFieldWithResolve extends EntityField{
    private String fetchUrl;
    private String parentType;
    private String loader;
    private List<EntityField> args;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EntityFieldWithResolve that = (EntityFieldWithResolve) o;

        if (fetchUrl != null ? !fetchUrl.equals(that.fetchUrl) : that.fetchUrl != null) return false;
        if (parentType != null ? !parentType.equals(that.parentType) : that.parentType != null) return false;
        if (loader != null ? !loader.equals(that.loader) : that.loader != null) return false;
        return args != null ? args.equals(that.args) : that.args == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fetchUrl != null ? fetchUrl.hashCode() : 0);
        result = 31 * result + (parentType != null ? parentType.hashCode() : 0);
        result = 31 * result + (loader != null ? loader.hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntityFieldWithResolve{" +
                "fetchUrl='" + fetchUrl + '\'' +
                ", parentType='" + parentType + '\'' +
                ", loader='" + loader + '\'' +
                ", args='" + args + '\'' +
                '}';
    }

    public String getFetchUrl() {
        return fetchUrl;
    }

    public void setFetchUrl(String fetchUrl) {
        this.fetchUrl = fetchUrl;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public String getLoader() {
        return loader;
    }

    public void setLoader(String loader) {
        this.loader = loader;
    }

    public List<EntityField> getArgs() {
        return args;
    }

    public void setArgs(List<EntityField> args) {
        this.args = args;
    }
}
