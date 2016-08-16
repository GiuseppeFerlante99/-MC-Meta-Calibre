/*
Author:Giuseppe Ferlante 
License: Gnu/GPL 
Version: 1.0 Beta 2
Title : MC-Meta-Calibre
 */
package MetaData;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author Giuseppe
 */
public class processIO {
    private String[] metaDati;
    private String calibreLibrary =System.getProperty("user.home")+"\\Documents\\Calibre Library";
    private String pathDB = calibreLibrary+"\\metadata.db";
    private String id;
    private Connection conn = null;
    
    public processIO(String[] metaDati)throws IDBookNotFound{
        try{
            Class.forName("org.sqlite.JDBC");
            /*Con questo forName registro i dRIVER invocando il metodo static  dentro la classe JDBC
             *  
             */
            this.setMeta(metaDati);
            this.conn=DriverManager.getConnection("jdbc:sqlite:"+this.pathDB);
            if((this.id = this.recognitionBook())==null){
                throw new IDBookNotFound(this.metaDati[0]);
            }
            this.setCover();
            
            
        }
        catch(Exception exc){
            exc.printStackTrace();
        }
    }
    public void setPathDatabase(String pathDatabase){
        this.pathDB=pathDatabase;
    }
    public void setMeta(String[] metaDati) throws Exception{
        if(metaDati.length==6){this.metaDati=metaDati;}
        else{throw new Exception("metaDati Array not valid");
        }
    }
    public void setCalibreLibraryPath(String path){
        this.calibreLibrary=path;
    }
    
    private void setCover() throws  FileNotFoundException, IOException, SQLException{
        try{
            ReadableByteChannel ch =  Channels.newChannel(new FileInputStream("cover.jpg"));
            System.out.println(this.calibreLibrary+"\\"+this.pathBook());
            
            FileOutputStream file = new FileOutputStream(this.calibreLibrary+"\\"+this.pathBook()+"\\cover.jpg",false);
            file.getChannel().transferFrom(ch, 0,Long.MAX_VALUE );
        }catch(FileNotFoundException exc){
            exc.printStackTrace();
        }catch(IOException exc){
            exc.printStackTrace();
            
        }catch(SQLException exc){
            exc.printStackTrace();
        }
    }
    
    public void closeConnection() throws SQLException{
        try{
            this.conn.close();
        }
        catch(SQLException exc){
            exc.printStackTrace();
        }
    }
    
    /**
     * 
     * @return la stringa dell'ID  del libro 
     * @throws SQLException 
     */
    private String recognitionBook() throws SQLException {
        try{
            Statement dich = this.conn.createStatement();
            ResultSet result = dich.executeQuery("select * from books");
            while(result.next()){
               
                if(MetaDataLibrary.percentageString(result.getString("title"),this.metaDati[0])>35){
                    return result.getString("id");
                }
            }
            
        }catch(SQLException exc){
            exc.printStackTrace();
            return null;
        }
        return null;
        
    }
    
    /**
     * 
     * @return ritorna se esiste la path del libro trovata dentro il database, non è completa ma solo parte del percorso CALIBRE.
     * @throws SQLException 
     */
    private String pathBook() throws SQLException{
        try{
            PreparedStatement stat = conn.prepareStatement("select path from books where id = ?" );
            stat.setString(1, this.id);
            ResultSet result = stat.executeQuery();
            System.out.println(result.toString());
            return result.getString("path");
            
        }catch(SQLException exc){
            exc.printStackTrace();
            return null;
        }
    }
    
	/**
	@return true se i dati nel database verranno aggiornati con i nuovi metadati altriemnti false
	*/
    public boolean updateAll()throws ClassNotFoundException,SQLException{
        String title = "";
        String id = null;
        boolean ver = false;
        
        try{
            
            //non capito 
       //     System.out.println("connessione avvenuta con successo");
            Statement stat = this.conn.createStatement();
            PreparedStatement statPrepared;
            try{
                stat.executeUpdate("DROP TRIGGER IF EXISTS books_insert_trg ");//elimino i trigger per evitare title_sort funzione interna 
                stat.executeUpdate("DROP TRIGGER IF EXISTS books_update_trg");
            }
            catch(SQLException exc){
                exc.printStackTrace();
            }    
            ResultSet result = stat.executeQuery("select * from books");
            while(result.next()){
                if((id=recognitionBook())!=null)
                {
                    ver = true;
                  
                    statPrepared = conn.prepareStatement("UPDATE books set title = ? , author_sort = ?, has_cover=1, isbn=? where id=?");
                    statPrepared.setString(1, this.metaDati[0]);
                    statPrepared.setString(2, this.metaDati[2]);
                    statPrepared.setString(3, this.metaDati[4]);
                    statPrepared.setString(4, id);
                    statPrepared.executeUpdate();
                    
                    statPrepared = conn.prepareStatement("INSERT OR IGNORE INTO comments(book,text) VALUES(?, ?);");
                    statPrepared.setString(1, id);
                    statPrepared.setString(2, this.metaDati[1]);
                    statPrepared.executeUpdate();
                    
                    statPrepared = conn.prepareStatement("UPDATE comments set text=? where book=?");
                    statPrepared.setString(1, this.metaDati[1]);
                    statPrepared.setString(2, id);
                    statPrepared.executeUpdate();
  
                    break;
                }
            }
            return ver;
            

        }
        catch(SQLException exc){
            exc.printStackTrace();
        }
        return ver;
    }
  
    protected  void writeinFile(String nomeFile, String formato,String contenuto,int BUFFER_LENGTH)throws FileNotFoundException,IOException,IndexOutOfBoundsException{
        char[] chCon = contenuto.toCharArray();
        int offsetIn=0 ;
        System.out.println(chCon.length);
        FileOutputStream file = new FileOutputStream(nomeFile+formato);
        OutputStreamWriter outputStream = new OutputStreamWriter(file);
        BufferedWriter bufferStreamWriter = new BufferedWriter(outputStream);
        try{
            for(offsetIn=0;offsetIn<chCon.length-1;offsetIn+=BUFFER_LENGTH)
            {
                bufferStreamWriter.write(chCon, offsetIn, offsetIn+BUFFER_LENGTH);
            }
        }catch(IndexOutOfBoundsException exc){
        //    System.out.println("SONO QUI->"+offsetIn);
            bufferStreamWriter.write(chCon,offsetIn,chCon.length-offsetIn-1);
            
        }
        bufferStreamWriter.close();
    }
   
}
