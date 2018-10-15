package buddy.easeshare;

import java.util.LinkedHashMap;

public class GenerateUsername {

    private String username;
    private LinkedHashMap<Integer, Character> numtochar = new LinkedHashMap<>();
    public String generate(String contact){

        char x = 'a';

        for(int i=0;i<=9;i++){
            numtochar.put(i,x);
            x++;
        }

        for(int i = 0 ; i < contact.length() ; i++){
            username = username + String.valueOf(numtochar.get(Character.getNumericValue(contact.charAt(i))));
        }

        return username;
    }
}
