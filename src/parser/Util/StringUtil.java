package parser.Util;

public class StringUtil {


    public static String firstToLowerCase(String string) {

        return string.replaceFirst(string.substring(0, 1), string.toLowerCase().substring(0, 1));

    }

    public static String firstToUpperCase(String string) {
        return string.replaceFirst(string.substring(0, 1), string.toUpperCase().substring(0, 1));

    }

    public static  String singularToPlural(String singular){
        String plural = "";
        if(singular.endsWith("y"))
            plural = singular.replaceAll("y$", "ies");
        else if (checkSes(singular))
            plural = singular + "es";
        else
            plural = singular + "s";


        return plural;
    }

    private static boolean checkSes(String singular){
        if(singular.endsWith("ch")||singular.endsWith("s")||singular.endsWith("sh")||singular.endsWith("x")
                ||singular.endsWith("z"))
            return true;
        else
            return false;
    }
}
