import java.util.HashMap;
import java.util.Random;

public class stockValue {
    HashMap<String,Float> StockValueMap=new HashMap<String,Float>();
    String[] companies= {"Microsoft","Kingfisher","Sun Pharma","Google","Indian Oils","Reliance","Facebook","Polo Steels","United Breweries","Alphabet","Tesla","TISCO"};
    
    public stockValue()
    {
        int i=0,n=companies.length;
        while(i<n)
        {
            Random rand= new Random();
            StockValueMap.put(companies[i],1000*rand.nextFloat());
            i++;
        }
    }
}