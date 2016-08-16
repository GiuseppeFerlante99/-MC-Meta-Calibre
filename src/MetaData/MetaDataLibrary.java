/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MetaData;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;

import java.nio.channels.ReadableByteChannel;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Giuseppe
 */
public class MetaDataLibrary  {
    
    private String nomeLibro,autore,descrizione;
    private final String AMAZON = "https://www.amazon.it/s/ref=nb_sb_noss_2?__mk_it_IT=%C3%85M%C3%85%C5%BD%C3%95%C3%91&url=search-alias%3Dstripbooks&field-keywords=";
    public MetaDataLibrary(String nomeLibro, String autore) throws Exception{
        try{
            setNomeLibro(nomeLibro);
            setAutore(autore);
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }

    private void setNomeLibro(String nomeLibro) throws Exception {
        if(nomeLibro.length() > 0){
            this.nomeLibro = nomeLibro;
        }
        else{
            throw new Exception("Nome libro inesistente");
        }
    }

    private void setAutore(String autore) throws Exception {
        if(autore.length() > 0 ){
            this.autore = autore;
        }
        else{
            throw new Exception("NOME autore inesistente");
        }
        
    }

    public String getNomeLibro(){
        return this.nomeLibro;
    }
    
    public String getAutore(){
        return this.autore;
    }

    /**
        Processa s2 Stringa immessa dall utente con s1 , e restituisce la percentuale rapportata a s2.
        la percentuale è gli elementi trovati uguali in s1 / la lunghezza di s2.( xkè s1 può contenere publicità) 
    */
    protected static int percentageString ( String s1 , String s2){
        // s1 processed jsoup
        // s2 user
        int elementEquals = 0, pos = 0;
     //   System.out.println("["+s1+","+s2+"]");
        String[] s2Array = s2.toLowerCase().split(" ");
        s1 =s1.toLowerCase()+ " ";
        
        for(String element:s2Array){
            try {    
          //      System.out.println("elemento processato: "+element);
                if(   ((pos=s1.indexOf(element))!=-1)  && (s1.charAt(pos+element.length())==' ')  && (s1.charAt(pos-1)==' ')   ){
                    elementEquals++;
            //        System.out.println("+1:"+pos);
                }
            }
            catch(StringIndexOutOfBoundsException exc){
                elementEquals++;
              //  System.out.println("ECCEZZIONE +1:"+pos);
            }
        }
       
        
       // System.out.println("Elementi uguali trovati ->"+elementEquals);
       if(elementEquals>0){ //BUG 2147483647 RISOLTO
           return (int)(s2.split(" ").length/(float)(elementEquals) )*100;
       }
       else{
           return 0;
       }
        
    }
    
    
    /**
     * 
     * @param s1 
     * @param s2
     * @return confronta s2 con s1 ,se anche una parola di s2 non è uguale con s1 return false
     */
    private boolean getAllContentString(String s1 , String s2){
      //  System.out.println("PARAMETRI ["+s1+","+s2+"]");
        String[] s2Arr=s2.toLowerCase().split(" ");
        s1 = s1.toLowerCase();
        for(String element : s2Arr){
            if(s1.contains(element)==false){
                return false;
            }
        }
        return true;
        
    }
    
	
	/**
	Scarica dal link il file e lo salva.
	*/
    public boolean downloader(String link,String nomeFile) throws IOException{
  //      System.out.println("["+link+","+formato+"]");
    try{
        URL downloadFile = new URL (link);
        FileOutputStream file = new FileOutputStream(nomeFile);
        ReadableByteChannel p = Channels.newChannel(downloadFile.openStream());
        file.getChannel().transferFrom(p, 0, Long.MAX_VALUE);
        return true;
    }catch(Exception exc )
    {
        return false;
    }
    }
 
    
    /*
    public BufferedImage compareImage(BufferedImage image1, String urlImage2) throws IOException{
    	try{
	    	BufferedImage image2Obj = ImageIO.read(new URL(urlImage2));
	    	if((image1.getHeight()*image1.getWidth())>=(image2Obj.getHeight()*image2Obj.getWidth())){
	    		return image1;
	    	}
	    	else{
	    		return image2Obj;
	    	}

    	}catch(IOException exc){
	    	exc.printStackTrace();
	    	return null;
	    }
    }
    */
/*
    public BufferedImage base64toBufferedImage(String base64)throws IOException{
    	try{
    		Decoder base64Decode = Base64.getDecoder();
	    	byte[] result = base64Decode.decode(base64);
	    	return ImageIO.read(new ByteArrayInputStream(result));
    	}catch(IOException exc){
    		exc.printStackTrace();
    		return null;
    	}
    }
    */
	
	
	/**
	legge una paggina web 
	@return : ritorna String della pagina web.
	*/
    private String readPage(String urlPage)throws MalformedURLException, IOException{
        String temporanea ;
        StringBuilder buffer = new StringBuilder(2048);
        //String Builder non syncronized and fast , StringBuffer Sincronizhed
        try(InputStream page  = (new URL(urlPage)).openStream()){
            InputStreamReader pageInputStreamReader = new InputStreamReader(page);
            BufferedReader bufferReaderPage = new BufferedReader(pageInputStreamReader);
            while((temporanea = bufferReaderPage.readLine())!=null){
               buffer.append(temporanea);
            }
            return buffer.toString();
        }
        catch(IOException exc){
            throw new IOException("ERRORE NEL COLLEGAMENTO");
            
        }
        
    }
	
	
	/**
	Da container cerca findString e partendo dalla posizione trovata di quest'ultima cerca charachter 
	Es. "ciao":{bene pippo giuseppe, allora materasso scala,} con findString="pippo" e charachter="," ritorna:
		{5,19}
		5=p
		19= ","
	@return: int1: posizione di inizio di findString in container , int2:posizione di charachter 
	*/
    private int[] indexEndLine(String container, String findString,char charachter)
    {
        System.out.println("->"+findString);
        char[] containerChar;
        int indexInit = container.indexOf(findString);
    //    System.out.println("-> trovato inizio di "+findString+":->"+indexInit);
      //  container = container.substring(indexInit);
  //      System.out.println("COMTAIENR->"+container);
        containerChar = container.toCharArray();
        
        System.out.println(container.length()+", index init:"+indexInit);
        for(int x = indexInit; x<container.length(); x++){
     //       System.out.println("STO PROCESSANDO ["+containerChar[x]+","+charachter+"]");
            if(containerChar[x]==charachter){
          //      System.out.println("FINE TROVATA->"+x);
                return new int[]{indexInit,x};
            } 
        }
        return null;
    }
    
    /**
     * 
     * @return String[titolo,deescrizione, autori,ISBN-10,ISBN-13,"true" or "false" depened Exists "cover.jpg" or not.]
     * @throws MalformedURLException problemi di connessione con Amazon .
     * @throws IOException 
     */
    public String[] requestMetaData() throws MalformedURLException, IOException,ParseException{
        String title="",autori="",link="";
        String[] linkImage= new String[2];
        try{
            Document doc = Jsoup.parse(this.readPage(this.AMAZON+this.nomeLibro.replace(" ", "+")));
            Elements booksContainer= doc.getElementsByAttributeValue("class", "s-result-item celwidget");
         
            //getElementBYClass not working 
            for(Element bookContainer : booksContainer){
                title  = bookContainer.getElementsByAttributeValue("class", "a-size-medium a-color-null s-inline  s-access-title  a-text-normal").text();
                autore=bookContainer.getElementsByAttributeValue("class", "a-size-small a-color-secondary").text();
                
                //         System.out.println("Title: "+title+"");
                
          //      autore = autore.contains("Ulteriori") ? autore.split("Ulteriori")[0]: autore;
                if(autore.contains("Ulteriori")){
                    autore= autore.split("Ulteriori")[0];

                }
                if((title.length()>2) && ( this.percentageString(title,this.nomeLibro)>15) && 
                   (this.getAllContentString(autore, this.autore))==true){
                    System.out.println("--------------------------------------------------");
                    link=bookContainer.getElementsByAttributeValue("class", "a-link-normal s-access-detail-page  a-text-normal").attr("href");
                    doc = Jsoup.parse(this.readPage(link));
                    Elements container = doc.getElementsByAttributeValue("data-feature-name", "booksTitle");
                    autori= container.first().getElementsByAttributeValue("class", "a-section a-spacing-micro bylineHidden feature").text().split("&")[0];                    
                    System.out.println("AUTORE AGGIORNATO :"+autori);
                    
               //   String linkImage = doc.getElementsByAttributeValue("id", "imgGalleryContent").first().getElementsByAttributeValue("id","igImage").attr("src");
               //     System.out.println("SIZE:"+doc.getElementsByAttributeValue("id", "igImage").size());
                    
                    String containerImageFunction = doc.getElementById("booksImageBlock_feature_div").getElementsByTag("script").toString();
                //    System.out.println(containerImageFunction);
                    System.out.println(containerImageFunction);
                    int[] indexEndInit = indexEndLine(containerImageFunction, "imageGalleryData",';');
                    System.out.println("INDEX 1->"+indexEndInit[1]);
                    String containerImageString = containerImageFunction.substring(indexEndInit[0]+20,indexEndInit[1]-68);
                    System.out.println(containerImageString);
                    try{
                        JSONParser parser = new JSONParser();
                        JSONObject ogg;
                        JSONArray arr = (JSONArray)(parser.parse(containerImageString));
                        
                        for(int x=0 ; x<arr.size(); x++){
                            ogg = (JSONObject)arr.get(x);
                            linkImage[x]=(String)ogg.get("mainUrl");
                        }
                    }catch(ParseException exc){
                        exc.printStackTrace();
                    }
                    
                    for(String element : linkImage){
                        System.out.println("LN:"+element);
                    }
                    container = doc.getElementsByAttributeValue("id", "detail_bullets_id").first().getElementsByTag("li");
                    return new String[]{title, doc.getElementsByAttributeValue("id", "bookDescription_feature_div").first().getElementsByTag("noscript").text(), autori,container.get(4).text(),container.get(5).text(),
                                        downloader(linkImage[0], "cover.jpg")?"true":"false"};
                    
                }
              
                }
            
        
           return null;
           
            
        }
        catch(MalformedURLException exc){
          exc.printStackTrace();
          return null;
        }
        catch(IOException exc){
            exc.printStackTrace();
            System.err.println("ERRORE NEL COLLEGAMENTO , RIPROVARE PERFAVORE");
            return null;
        }
       
    }
    
}
