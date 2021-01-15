package server;


import com.google.gson.Gson;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import oracle.jdbc.driver.OracleDriver;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Windows7
 */
public class Server extends javax.swing.JFrame {
    
    ServerSocket serverSocket;
    Socket servSock;
    Thread th = null;
    ////////////db//////////////
    static Connection con;
    static PreparedStatement pst;
    static ResultSet rs;
    static ResultSet rs2;

    /**
     * Creates new form Server
     */
    public Server() {
        initComponents();
        btnStop.setEnabled(false);
        btnAddItem.setEnabled(false);
        this.setResizable(false);
        this.setSize(400, 300);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);


        
    }
    
    // start server
    public void connServ() {
        try {
            serverSocket = new ServerSocket(5005);
            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Socket s;
                        try {
                            s = serverSocket.accept();
                            new ClientHandler(s);
                        } catch (SocketException ex) {
                            th.stop();
                            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            th.start();
        } catch (IOException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // connection with DB
    public void connDB() {
        try {
            DriverManager.registerDriver(new OracleDriver());
            con = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:xe", "java", "java");
            con.setAutoCommit(false);  
            pst = con.prepareStatement("SELECT id FROM usr WHERE ROWNUM = 1 ORDER BY id DESC", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException ex) {
            
            //ex.printStackTrace();
        }
    }
    

    static UserInfo logMsg(UserInfo usr) {
        try {
            pst = con.prepareStatement("SELECT * FROM usr where usr_name = ? and pw = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr.getUsrName());
            pst.setString(2, usr.getPw());
            rs = pst.executeQuery();
            
            if (rs.next()) {
                usr.setResult("success");
            // set user details
            usr.setUsrName(rs.getString(2));
            usr.setPw(rs.getString(3));
            usr.setEmail(rs.getString(4));
            usr.setFname(rs.getString(5));
            usr.setLname(rs.getString(6));
            String usr_id = rs.getString(1);
            rs.close();
            pst.close();
            // Select and set user's wishlist details
            pst = con.prepareStatement("select item.* , w.total_paid \n" +
                                    "from item, wishlist w\n" +
                                    "where item.id = w.item_id\n" +
                                    "    and w.usr_id = ?\n "
                    + "and completed = 0 \n" , ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //rs.next();
            pst.setString(1, usr_id);
            
            rs = pst.executeQuery();
            Vector <ProdInfo> prodWishList = new Vector();
            while(rs.next()){
            ProdInfo prod= new ProdInfo();
                
                prod.setName(rs.getString(2));
                prod.setPrice(rs.getInt(3));
                prod.setQty(rs.getInt(4));
                prod.setDesc(rs.getString(5));
                prod.setImg(rs.getString(6));
                prod.setPaid(rs.getInt(7));
                
                prodWishList.add(prod);
            }
            //System.out.println(prodWishList);
            usr.setWishList(prodWishList);
            //System.out.println(usr.getWishList());
            //System.out.println("I added products to my wish list");
            rs.close();
            pst.close();
            
            //Select and set available products
            pst = con.prepareStatement("SELECT * FROM item", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                       
            rs = pst.executeQuery();
            Vector <ProdInfo> prodAvailableList = new Vector();
            while(rs.next()){
            ProdInfo prod= new ProdInfo();
                
                prod.setName(rs.getString(2));
                prod.setPrice(rs.getInt(3));
                prod.setQty(rs.getInt(4));
                prod.setDesc(rs.getString(5));
                prod.setImg(rs.getString(6));
                prodAvailableList.add(prod);
            }
            usr.setAvailableProds(prodAvailableList);
            rs.close();
            pst.close();
            
            // Select and set user's friend list
            pst = con.prepareStatement("SELECT usr_name FROM usr where usr.id in (Select friend_id from friend where usr_id = ? and friend_status = 0) or usr.id in (Select usr_id from friend where friend_id = ? and friend_status = 0)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            pst.setString(2, usr_id); 
            rs = pst.executeQuery();
            Vector <String> friendList = new Vector();
            while(rs.next()){

                friendList.add(rs.getString(1));
            }
            usr.setAprvFriends(friendList);
            rs.close();
            pst.close();
            
            // Select and set friend requests
            pst = con.prepareStatement("SELECT US.USR_NAME\n"
            + "FROM USR US \n"
            + "WHERE US.ID IN\n"
            + "(SELECT FRIEND_ID\n"
            + "FROM FRIEND F, USR US\n"
            + "WHERE F.USR_ID =US.ID and F.FRIEND_STATUS =1 AND US.USR_NAME = ? )\n"
            + "", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr.getUsrName());
            rs = pst.executeQuery();
            Vector<String> PendFriend = new Vector();
           
            while (rs.next()){
            

            PendFriend.add(rs.getString(1));

            }
            usr.setPendFriends(PendFriend);
            rs.close();
            pst.close();
            
            // retrive notifications of completed gifts you wished for // fill completed list 
            pst = con.prepareStatement("select *  from  item where id in(select item_id from wishlist where usr_id = ? and completed = 1)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pst.setString(1, usr_id);
                rs = pst.executeQuery();
                Vector<ProdInfo> completedProds = new Vector();
                Vector<String> contributedfrieds = new Vector();
                ProdInfo prod = new ProdInfo();
                while (rs.next()) {

                    
                    prod.setName(rs.getString(2));
                    prod.setPrice(rs.getInt(3));
                    prod.setQty(rs.getInt(4));
                    prod.setDesc(rs.getString(5));
                    prod.setImg(rs.getString(6));
                    pst = con.prepareStatement(" select distinct us.usr_name   from usr us , item i , payment pt   "
                            + "where I.ID = PT.ITEM_ID and  US.ID = PT.USR_ID and "
                            + "friend_id =? and I.ID = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pst.setString(2, rs.getString(1));
                    pst.setString(1, usr_id);
                    
                    rs2 = pst.executeQuery();
                    completedProds.add(prod);
                    
                     while (rs2.next()) {
                            contributedfrieds.add(rs2.getString(1));
                     }
                     prod.setContributedFriends(contributedfrieds);
                    
                }
                
                usr.setCompletedProds(completedProds);
                
            rs.close();
            pst.close();
            // retrive notifications of completed gifts you contributed in // fill completed contributions list
            pst = con.prepareStatement("select usr_id, item_id\n" +
                                        "from wishlist\n" +
                                        "where completed =1", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                       
            rs = pst.executeQuery();
            Vector <ContrDetails> contrList = new Vector();
            while(rs.next()){
                    pst = con.prepareStatement("select distinct p.usr_id, usr.usr_name as friend_name , item.name as product_name\n" +
                                                "from payment p, item, usr\n" +
                                                "where usr.id = p.FRIEND_ID\n" +
                                                "       and item.id = p.ITEM_ID\n" +
                                                "        and p.friend_id = ?\n" +
                                                "        and p.item_id =?\n" +
                                                "        and p.usr_id = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                       
                    pst.setString(1, rs.getString(1));
                    pst.setString(2, rs.getString(2));
                    pst.setString(3, usr_id);
                    rs2 = pst.executeQuery();
                    
                    while(rs2.next()){
                    ContrDetails contr = new ContrDetails();
                    contr.setFriendName(rs2.getString(2));
                    contr.setProdName(rs2.getString(3));
                    contrList.add(contr);
                    System.out.println("Contribution Details contr "+ contr.getFriendName()+" "+contr.getProdName());
                    
                    }
                    
                    
                    
            }
            usr.setCompletedContributions(contrList);
            rs.close();
            pst.close();
            
            // Retriving user credit 
            pst = con.prepareStatement("SELECT CREDIT FROM usr where usr.id  = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pst.setString(1, usr_id);
                rs = pst.executeQuery();
                //UserInfo contribute = new UserInfo();
                while (rs.next()) {

                    // contribute.setCredit(rs.getInt(1));
                    usr.setCredit(rs.getInt(1));
            
                }
                        rs.close();
            pst.close();
            
            }
            
            else {
                usr.setResult("fail");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return usr;
    }

    static UserInfo regMsg(UserInfo data) {
        
        try {
            // get next id
            pst = con.prepareStatement("SELECT id FROM usr WHERE ROWNUM = 1 ORDER BY id DESC", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = pst.executeQuery();
            String id = "1";
            if (rs.next()) {
                int temp = rs.getInt("id");
                temp++;
                id = String.valueOf(temp);
            }
            rs.close();
            pst.close();
            // insert
            pst = con.prepareStatement("INSERT INTO usr VALUES ( ? , ? , ? , ? , ? , ?,0,'' )", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, id);   pst.setString(2, data.getUsrName());
            pst.setString(3, data.getPw());   pst.setString(4, data.getEmail());
            pst.setString(5, data.getFname());   pst.setString(6, data.getLname());
            pst.executeUpdate();
            con.commit();
            //System.out.println("Row inserted");
            data.setResult("success");
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            //data.setResult("fail");
            
            data.setResult(Integer.toString(ex.getErrorCode()));
            System.out.println("Error code: "+ex.getErrorCode());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        //data.reptype = "repreg";
        
        return data;

    }

       static UserInfo fWishMsg(UserInfo usr){
           String usr_id=null;
        String usrName = usr.getUsrName();
            Vector <ProdInfo> prodList = new Vector();
        try {
            pst = con.prepareStatement("SELECT id FROM usr WHERE usr_name = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usrName);
            rs = pst.executeQuery();
            if(rs.next()){usr_id = rs.getString(1);}
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("SELECT item.* FROM wishlist, item WHERE usr_id = ? and item.id = wishlist.item_id and completed = 0", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            
            rs = pst.executeQuery();
            while(rs.next()){
            ProdInfo prod= new ProdInfo();
                
                prod.setName(rs.getString(2));
                prod.setPrice(rs.getInt(3));
                prod.setQty(rs.getInt(4));
                prod.setDesc(rs.getString(5));
                prod.setImg(rs.getString(6));
                prodList.add(prod);
            }
            usr.setWishList(prodList);
            rs.close();
            pst.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.print(usr);
        return usr;
    }
       static UserInfo rmFriend(UserInfo data){
        try {
            //System.out.println();
            String usr_id = null, friend_id = null;
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getFriendName());
            rs = pst.executeQuery();
            if(rs.next())
            usr_id = rs.getString(1);
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            if(rs.next())friend_id = rs.getString(1);
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("DELETE FROM FRIEND \n" +
                                        "WHERE USR_ID in (?,?) AND FRIEND_ID in (?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            pst.setString(2, friend_id);
            pst.setString(3, usr_id);
            pst.setString(4, friend_id);
            //System.out.println(data.getUsrName());
            //System.out.println(data.getFriendName());
            //System.out.println(pst);
            
            pst.executeUpdate();
            con.commit();
            //System.out.println("Row deleted");
            //System.out.println(data.getAprvFriends());
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        data.setResult("success");
            return data;
       }
        static UserInfo friendRequest(UserInfo data){
        try {
            String usr_id = null, friend_id = null;
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getFriendName());
            rs = pst.executeQuery();
            if(rs.next()){
            usr_id = rs.getString(1);}
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            if(rs.next()){friend_id = rs.getString(1);}
            rs.close();
            pst.close();
            pst = con.prepareStatement("insert into friend values (?,?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            pst.setString(2, friend_id);
            pst.setString(3, "1");
            rs = pst.executeQuery();
            con.commit();
            
            
            data.setResult("success");
            rs.close();
            pst.close();
            
        } catch (SQLException ex) {
            data.setResult(Integer.toString(ex.getErrorCode()));
            System.out.println("Error code: "+ex.getErrorCode());
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
        }
        static UserInfo FDialogfriendRequest(UserInfo data){
           try {
              
            //String usr_id = null, friend_id = null;
               if(data.getFlagFriendReq()==true)
               {
                   
                   pst = con.prepareStatement("update friend set FRIEND_STATUS=0\n" +
                                               "WHERE FRIEND_ID=(\n" +
                                                    "SELECT FRIEND_ID\n" +
                                                     "FROM(\n" +
                                                            "SELECT US.USR_NAME as FRIEND_NAME ,US.ID AS FRIEND_ID\n" +
                                                            "FROM USR US \n" +
                                                            "WHERE US.ID IN\n" +
                                                                  "(SELECT FRIEND_ID\n" +
                                                                  "FROM FRIEND F, USR US\n" +
                                                                  "WHERE F.USR_ID =US.ID and F.FRIEND_STATUS =1 AND US.USR_NAME =?))\n" +
                                                                  "WHERE FRIEND_NAME=?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                  
                   pst.setString(1, data.getUsrName());
                    pst.setString(2, data.getFriendName());
                     pst.executeUpdate();
                     con.commit();
                     
                    // Select and set user's friend list
                    pst = con.prepareStatement("select usr.usr_name \n" +
                                                                "from usr\n" +
                                                                "where usr.id in (\n" +
                                                                "                        select usr_id \n" +
                                                                "                        from friend\n" +
                                                                "                        where friend_id in ( select usr.id\n" +
                                                                "                                                    from usr\n" +
                                                                "                                                    where usr_name = ? )\n" +
                                                                "                                  and friend_status = 0\n" +
                                                                "                        )\n" +
                                                                "            or usr.id in (\n" +
                                                                "                            select friend_id\n" +
                                                                "                            from friend\n" +
                                                                "                            where usr_id in (select usr.id\n" +
                                                                "                                                    from usr\n" +
                                                                "                                                    where usr_name = ? )\n" +
                                                                "                                      and friend_status = 0\n" +
                                                                "                            )", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pst.setString(1, data.getUsrName());
                    pst.setString(2, data.getUsrName()); 
                    rs = pst.executeQuery();
                    Vector <String> friendList = new Vector();
                    while(rs.next()){

                        friendList.add(rs.getString(1));
                    }
                    data.setAprvFriends(friendList);
                    rs.close();
            pst.close();
            // Select and set friend requests
            
            pst = con.prepareStatement("SELECT US.USR_NAME\n"
            + "FROM USR US \n"
            + "WHERE US.ID IN\n"
            + "(SELECT FRIEND_ID\n"
            + "FROM FRIEND F, USR US\n"
            + "WHERE F.USR_ID =US.ID and F.FRIEND_STATUS =1 AND US.USR_NAME = ? )\n"
            + "", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            Vector<String> PendFriend = new Vector();
           
            while (rs.next()){
            PendFriend.add(rs.getString(1));

            }
            data.setPendFriends(PendFriend);
               }
               
               
               else if(data.getFlagFriendReq()==false)
               {
                   pst = con.prepareStatement("DELETE FROM friend \n" +
                                               "WHERE USR_ID= (select ID from usr where USR_NAME =?)\n" +
                                                     "and  FRIEND_ID=(\n" +
                                                                            "SELECT FRIEND_ID\n" +
                                                                            "FROM(\n" +
                                                                                    "SELECT US.USR_NAME as FRIEND_NAME ,US.ID AS FRIEND_ID\n" +
                                                                                    "FROM USR US \n" +
                                                                                    "WHERE US.ID IN\n" +
                                                                                                     "(SELECT FRIEND_ID\n" +
                                                                                                     "FROM FRIEND F, USR US\n" +
                                                                                                     "WHERE F.USR_ID =US.ID and F.FRIEND_STATUS =1 AND US.USR_NAME =?))\n" +
                                                                            "WHERE FRIEND_NAME=?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                   pst.setString(1, data.getUsrName());
                  pst.setString(2, data.getUsrName());
                    pst.setString(3, data.getFriendName());
                     pst.executeUpdate(); 
                     con.commit();
            
            // Select and set friend requests

            pst = con.prepareStatement("SELECT US.USR_NAME\n"
            + "FROM USR US \n"
            + "WHERE US.ID IN\n"
            + "(SELECT FRIEND_ID\n"
            + "FROM FRIEND F, USR US\n"
            + "WHERE F.USR_ID =US.ID and F.FRIEND_STATUS =1 AND US.USR_NAME = ? )\n"
            + "", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            Vector<String> PendFriend = new Vector();
           
            while (rs.next()){
            PendFriend.add(rs.getString(1));

            }
            data.setPendFriends(PendFriend);
            
               }
               rs.close();
            pst.close();
            data.setResult("success");
            
            
        } catch (SQLException ex) {
            data.setResult("fail");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
        return data;
       }
        static UserInfo contribute(UserInfo data) {
        try {
            System.out.println(data.getContribution().getContrAmount());
            String usr_id = null, friend_id = null, item_id = null;
            
            pst = con.prepareStatement("SELECT id FROM payment WHERE ROWNUM = 1 ORDER BY id DESC", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = pst.executeQuery();
            String id = "1";
            if (rs.next()) {
                int temp = rs.getInt("id");
                temp++;
                id = String.valueOf(temp);
            }
            
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            System.out.println(data.getUsrName());
            rs = pst.executeQuery();
            if (rs.next()) {
                usr_id = rs.getString(1);
            }
            rs.close();
            pst.close();
                pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pst.setString(1, data.getContribution().getFriendName());
                rs = pst.executeQuery();
                if (rs.next()) {
                    friend_id = rs.getString(1);
                }
                rs.close();
            pst.close();
                pst = con.prepareStatement("select id from ITEM where name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pst.setString(1, data.getContribution().getProdName());
                rs = pst.executeQuery();
                if (rs.next()) {
                    item_id = rs.getString(1);
                }
                rs.close();
            pst.close();
                pst = con.prepareStatement("insert into PAYMENT (ID,USR_ID,FRIEND_ID,ITEM_ID,PAID) VALUES (?,?,?,?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pst.setString(1, id);
                pst.setString(2, usr_id);
                pst.setString(3, friend_id);
                pst.setString(4, item_id);
                pst.setString(5, String.valueOf(data.getContribution().getContrAmount()));
                rs = pst.executeQuery();
                con.commit();
                System.out.println("committttt");
                
                pst = con.prepareStatement("select PAID from payment where id = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
               pst.setString(1, id);
//                pst.setString(3, friend_id);
//                pst.setString(2, item_id);
                rs = pst.executeQuery();
              ContrDetails actualamount =  new ContrDetails();
                if (rs.next()) {
                    
                     actualamount.setActualAmount(rs.getInt(1));
                     data.setContribution(actualamount);
                }
                
               rs.close();
            pst.close();
                pst = con.prepareStatement("select credit from usr where id = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
               pst.setString(1, usr_id);
                 
                rs = pst.executeQuery();
              
                if (rs.next()) {
                    
                    
                     data.setCredit(rs.getInt(1));
                }
                rs.close();
            pst.close();
                data.setResult("success");
        
            

        } catch (SQLException ex) {
            data.setResult("fail");

            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
        static UserInfo FunAvailableItem(UserInfo data){
          
          try {
            String usr_id = null, AddNewItem = null;
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            if(rs.next()){
            usr_id = rs.getString(1);
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("SELECT ID FROM ITEM WHERE NAME= ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getNewProd().getName());
            rs = pst.executeQuery();
            if(rs.next())AddNewItem = rs.getString(1);
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("insert into WISHLIST values (?,?,?,?)", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            pst.setString(2, AddNewItem);
            pst.setString(3, "0");
            pst.setString(4, "0");
            rs = pst.executeQuery();
            con.commit();
            
            
            
            data.setResult("success");
            }
            
        } catch (SQLException ex) {
            data.setResult("fail");
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
      }
        
    static UserInfo FunRemoveItem(UserInfo data){
           System.out.println("enter in FunRemoveItem");
           try {
            String usr_id = null, removeItem = null;
            pst = con.prepareStatement("select id from usr where usr_name = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            rs = pst.executeQuery();
            if(rs.next()){
            usr_id = rs.getString(1);
                System.out.println("user id =  " + usr_id);
            rs.close();
            pst.close();
            
            pst = con.prepareStatement("SELECT ID FROM ITEM WHERE NAME= ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getRemoveItem());
            rs = pst.executeQuery();
            if(rs.next())removeItem = rs.getString(1);
            System.out.println("item id =  " + removeItem);
            rs.close();
            pst.close();
            pst = con.prepareStatement("DELETE FROM WISHLIST WHERE USR_ID=? AND ITEM_ID=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usr_id);
            pst.setString(2, removeItem);
             pst.executeUpdate();
            con.commit();
            
            
            data.setResult("success");
            }
            
        } catch (SQLException ex) {
            data.setResult("fail");
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
       }
    
    static UserInfo updateCredit(UserInfo data){
            
         
        try {
            System.out.print(data.getCredit());
                pst = con.prepareStatement("update  usr set credit =? where usr_name = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
               pst.setString(2, data.getUsrName());
               pst.setInt(1, data.getCredit());
                 
              pst.executeUpdate();
              con.commit();
                //System.out.println("committttt");
               System.out.println(data.getUsrName());
                              System.out.println(data.getCredit());
               data.setResult("success");
              
               rs.close();
            pst.close();
            
        } catch (SQLException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
           data.setResult("fail");
        }
        return data; 
         
    }
        public void addExcelItems(Vector<Vector<String>> items_xlsx) {
        try {
            // get next id
            pst = con.prepareStatement("SELECT id FROM item WHERE ROWNUM = 1 ORDER BY id DESC", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = pst.executeQuery();
            String idStr = "1";
            int idInt = 1;
            if (rs.next()) {
                idInt = rs.getInt("id");
                idInt++;
                idStr = String.valueOf(idInt);
            }
            rs.close();
            pst.close();
            // insert data of excel file
            boolean inserted = true;
            
            for (Vector<String> row_vec : items_xlsx) {
                if (row_vec.size() == 5) { // 5 for image path
                    pst = con.prepareStatement("INSERT INTO item VALUES ( ? , ? , ? , ? , ? , ? )", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pst.setString(1, idStr);
                    pst.setString(2, row_vec.get(0));
                    pst.setString(3, row_vec.get(1));
                    pst.setString(4, row_vec.get(2));
                    pst.setString(5, row_vec.get(3));
                    pst.setString(6, row_vec.get(4)); // for image path
                    pst.executeUpdate();
                    idInt++;
                    idStr = String.valueOf(idInt);
                    rs.close();
            pst.close();
                }
                else {
                    JOptionPane.showMessageDialog(this, "Your file does not match the system");
                    inserted = false;
                    con.rollback();
                    break;
                }
            }
            if (inserted == true) {
                con.commit();
                JOptionPane.showMessageDialog(this, "Items addded to the system");
            }
        } catch (SQLException ex) {
            try {
                //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                con.rollback();
            } catch (SQLException ex1) {
                //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
            }
            JOptionPane.showMessageDialog(this, "Your file may contain dublicate items, or One or more of Items already exist");
        }
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        btnAddItem = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnAddItem.setText("AddItem");
        btnAddItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddItemActionPerformed(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(55, Short.MAX_VALUE)
                .addComponent(btnStart)
                .addGap(41, 41, 41)
                .addComponent(btnStop)
                .addGap(45, 45, 45)
                .addComponent(btnAddItem)
                .addGap(58, 58, 58))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(152, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart)
                    .addComponent(btnAddItem)
                    .addComponent(btnStop))
                .addGap(123, 123, 123))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
            
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        btnAddItem.setEnabled(true);
        // connect to DB
        connDB();
        // start the server
        connServ();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
       // TODO add your handling code here: 
        btnStop.setEnabled(false);
        btnAddItem.setEnabled(false);
        btnStart.setEnabled(true);
        // close Server connection with clients
        try {
            serverSocket.close();
        } catch (IOException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        ClientHandler.closeAll();
        // close DB connection
        try {
            pst.close();
            con.close();
        } catch (SQLException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
 
    }//GEN-LAST:event_btnStopActionPerformed

    private void btnAddItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddItemActionPerformed
        // TODO add your handling code here:
        
                                                   
        // TODO add your handling code here:
        JFileChooser openDia = new JFileChooser();
        int result = openDia.showOpenDialog(this);
        int row_count = -1; // to eliminate header
        Vector<String> row_item = new Vector<String>(); // for one item
        Vector<Vector<String>> items_xlsx = new Vector<Vector<String>>(); // for all items
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream fis = new FileInputStream(openDia.getSelectedFile()); // must be xlsx
                //creating Workbook instance that refers to .xlsx file  
                XSSFWorkbook wb = new XSSFWorkbook(fis);
                XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object  
                Iterator<Row> itr = sheet.iterator();    //iterating over excel file
                while (itr.hasNext()) {
                    Row row = itr.next();
                    Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column  
                    while (cellIterator.hasNext() && row_count > -1) {
                        Cell cell = cellIterator.next();
                        row_item.add(cell.toString()); // add cell
                    }
                    if (row_count > -1) {
                        Vector copy = new Vector(row_item);
                        items_xlsx.add(copy); // add row
                    }
                    row_item.clear();
                    row_count++;
                }
                addExcelItems(items_xlsx);
            } catch (NotOfficeXmlFileException ex) {
                JOptionPane.showMessageDialog(this, "Please choose .xlsx file extension");
            } catch (Exception e) {
                //e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Please choose .xlsx file extension");
            }
        }
    
    }//GEN-LAST:event_btnAddItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddItem;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}


class ClientHandler extends Thread {

    DataInputStream dis;
    PrintStream ps;
    Socket cs;
    static Vector<ClientHandler> clientsVector = new Vector<ClientHandler>();
    ////////////db//////////////
    Connection con;
    PreparedStatement pst;
    ResultSet rs;
    long thid;

    // constructor
    public ClientHandler(Socket cs) {
        try {
            this.cs = cs;
            dis = new DataInputStream(cs.getInputStream());
            ps = new PrintStream(cs.getOutputStream());
            ClientHandler.clientsVector.add(this);
            start();
            thid = this.getId();
            //System.out.println(this.getId());
        } catch (SocketException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // run method fot the thread
    public void run() {
        
        while (true) {
                try {
                String msg = dis.readLine();
                System.out.println(msg);
                if (msg != null) {
                    UserInfo data = new Gson().fromJson(msg, UserInfo.class);
                    data = handleMsg(msg);
                    long clientId = this.getId();
                    String msgbk = new Gson().toJson(data);
                    sendMessageToClient(msgbk, clientId);
                } else {
                    dis.close();
                    ps.close();
                    cs.close();
                    break;
                }

            } catch (SocketException ex) {
                try {
                    dis.close();
                } catch (IOException ex1) {
                //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
                ps.close();
                clientsVector.remove(this);
                stop();
                } catch (IOException ex) {
                //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }

    // server reply all
    void sendMessageToClient(String msg, long clientId) {
        for (ClientHandler ch : clientsVector) {
            
            if(ch.thid == clientId ){
            //System.out.println(clientId);
            ch.ps.println(msg);
            }
        }
    }
    
    // server close socket, and streams
    static void closeAll() {
        for (ClientHandler ch : clientsVector) {
            try {
                ch.stop();
                ch.cs.close();
                ch.dis.close();
                ch.ps.close();
            } catch (IOException ex) {
                //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // check type of msg
    UserInfo handleMsg(String msg) {

        UserInfo data = new Gson().fromJson(msg, UserInfo.class);
        
        switch (data.getType()) {
            case "log":
                data = Server.logMsg(data);
                break;
            case "reg":
                data = Server.regMsg(data);
                break;
            case "fWish":
                data = Server.fWishMsg(data);
                break;
            case "rmFriend":
                //System.out.println(data.getUsrName());
                System.out.println(data.getFriendName());
                //System.out.println(msg);
                data = Server.rmFriend(data);
                break;
            case "friendRequest":
                data = Server.friendRequest(data); 
                break;
            case "FDialogfriendRequest":
                data = Server.FDialogfriendRequest(data); 
                break;
            case "contribute":
                data = Server.contribute(data);
                break;
            case "typeAvailableItem":
                data = Server.FunAvailableItem(data);
                break;
            case "typeRemoveItem":
               data = Server.FunRemoveItem(data);
                break;
            case "updateCredit":
                data = Server.updateCredit(data);

                break;
            default:
            // code block
            
        }
        return data;
    }
}













