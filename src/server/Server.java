package server;


import com.google.gson.Gson;
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

    /**
     * Creates new form Server
     */
    public Server() {
        initComponents();
        btnStop.setEnabled(false);
        btnAddItem.setEnabled(false);
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
                            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            th.start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // connection with DB
    public void connDB() {
        try {
            DriverManager.registerDriver(new OracleDriver());
            con = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:xe", "java", "java");
            con.setAutoCommit(false);         
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    

    static UserInfo logMsg(UserInfo data) {
        try {
            pst = con.prepareStatement("SELECT * FROM usr where usr_name = ? and pw = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, data.getUsrName());
            pst.setString(2, data.getPw());
            rs = pst.executeQuery();
            if (rs.next()) {
                data.setResult("success");
            }
            else {
                data.setResult("fail");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
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
            // insert
            pst = con.prepareStatement("INSERT INTO usr VALUES ( ? , ? , ? , ? , ? , ? )", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, id);   pst.setString(2, data.getUsrName());
            pst.setString(3, data.getPw());   pst.setString(4, data.getEmail());
            pst.setString(5, data.getFname());   pst.setString(6, data.getLname());
            pst.executeUpdate();
            con.commit();
            System.out.println("Row inserted");
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        //data.reptype = "repreg";
        data.setResult("success");
        return data;

    }

       static UserInfo fWishMsg(UserInfo usr){
        String usrName = usr.getUsrName();
            Vector <ProdInfo> prodList = new Vector();
        try {
            pst = con.prepareStatement("SELECT id FROM usr WHERE usr_name = ? ", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, usrName);
            rs = pst.executeQuery();
            
            pst = con.prepareStatement("SELECT item.* FROM wishlist, item WHERE usr_id = ? and item.id = wishlist.item_id", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs.next();
            pst.setString(1, rs.getString(1));
            
            rs = pst.executeQuery();
            while(rs.next()){
            ProdInfo prod= new ProdInfo();
                
                prod.setName(rs.getString(2));
                prod.setPrice(rs.getInt(3));
                prod.setQty(rs.getInt(4));
                prod.setDesc(rs.getString(5));
            
                prodList.add(prod);
            }
            usr.setWishList(prodList);
            
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.print(usr);
        return usr;
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
            // insert data of excel file
            boolean inserted = true;
            for (Vector<String> row_vec : items_xlsx) {
                if (row_vec.size() == 4) { // 5 for image path
                    pst = con.prepareStatement("INSERT INTO item VALUES ( ? , ? , ? , ? , ? , null )", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    pst.setString(1, idStr);
                    pst.setString(2, row_vec.get(0));
                    pst.setString(3, row_vec.get(1));
                    pst.setString(4, row_vec.get(2));
                    pst.setString(5, row_vec.get(3));
                    //pst.setString(6, row_vec.get(4)); // for image path
                    pst.executeUpdate();
                    idInt++;
                    idStr = String.valueOf(idInt);
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
            System.out.println(this.getId());
        } catch (SocketException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // run method fot the thread
    public void run() {
        
        while (true) {
                try {
                String msg = dis.readLine();
                UserInfo data = new Gson().fromJson(msg, UserInfo.class);
                data = handleMsg(msg);
                // send msg back to all client
                //if(thid = )
                
                long clientId = this.getId();
                String msgbk = new Gson().toJson(data);
                System.out.println(msgbk);
                
                sendMessageToClient(msgbk, clientId);
                
                } catch (SocketException ex) {
                try {
                dis.close();
                } catch (IOException ex1) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
                ps.close();
                clientsVector.remove(this);
                stop();
                } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }

    // server reply all
    void sendMessageToClient(String msg, long clientId) {
        for (ClientHandler ch : clientsVector) {
            
            if(ch.thid == clientId ){
            System.out.println(clientId);
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
                
            default:
            // code block
            
        }
        return data;
    }
}












