package server;

import java.util.Vector;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kandil
 */

class ContrDetails{
    private String prodName;
    private int contrAmount;
    private int actualAmount;
    private String friendName;

    public int getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(int actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
    

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public int getContrAmount() {
        return contrAmount;
    }

    public void setContrAmount(int contrAmount) {
        this.contrAmount = contrAmount;
    }
    
}
class UserInfo {
    private String type, reptype; // type of streams
    private String result; // welcome back - wrong user name or pw
    private String usrName, pw, email, fname, lname; // attribute of user
    private Vector <ProdInfo> wishList; // itemms of user
    private ContrDetails contribution; // obj containing friendname, prodname, contribution amount
    private Vector <ProdInfo> availableProds; // a vector of the available products in the database
    private Vector <ProdInfo> completedProds; //  a vector of gifts that have been fullfilled
    private String friendName; // The name of the friend that user wants to add or remove
    private Vector <String> pendFriends; // Pending friends of user
    private Vector <String> aprvFriends; //  a vector of user's friends
    private Vector <String> completedContributions;
    private int credit; // user's credit or wallet
    private String addNewItem;
    private String RemoveItem;

    
    private boolean FlagFriendReq;
    

    public UserInfo() {
    }

    public UserInfo copy(UserInfo myInfo) {
        myInfo.setType(this.getType());
        
        myInfo.setUsrName(this.getUsrName());
        myInfo.setFname(this.getFname());
        myInfo.setLname(this.getLname());
        myInfo.setEmail(this.getEmail());
        myInfo.setWishList(new Vector(this.getWishList()));
        myInfo.setAvailableProds(new Vector(this.getAvailableProds()));
        myInfo.setPendFriends(new Vector(this.getPendFriends()));
        myInfo.setContribution(this.getContribution());
        myInfo.setAprvFriends(new Vector(this.getAprvFriends()));
        myInfo.setCompletedProds(new Vector(this.getCompletedProds()));
        myInfo.setCompletedContributions(new Vector(this.getCompletedContributions()));
        myInfo.setFriendName(this.getFriendName());
        myInfo.setCredit(this.getCredit());
        myInfo.setFlagFriendReq(this.getFlagFriendReq());
        myInfo.setAddNewItem(this.getAddNewItem());
        myInfo.setRemoveItem(this.getRemoveItem());
        
        return myInfo;
    }
    
     public boolean getFlagFriendReq() {
        return  FlagFriendReq;
     }
    public void setFlagFriendReq(boolean FFriendReq) {
         FlagFriendReq=FFriendReq;
    }
    
    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUsrName() {
        return usrName;
    }

    public void setUsrName(String usrName) {
        this.usrName = usrName;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public Vector <ProdInfo> getWishList() {
        return wishList;
    }

    public void setWishList(Vector <ProdInfo> wishList) {
        this.wishList = wishList;
    }

    public Vector <String> getPendFriends() {
        return pendFriends;
    }

    public void setPendFriends(Vector <String> pendFriends) {
        this.pendFriends = pendFriends;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReptype() {
        return reptype;
    }

    public void setReptype(String reptype) {
        this.reptype = reptype;
    }

    public ContrDetails getContribution() {
        return contribution;
    }

    public void setContribution(ContrDetails contribution) {
        this.contribution = contribution;
    }

    public Vector <ProdInfo> getAvailableProds() {
        return availableProds;
    }

    public void setAvailableProds(Vector <ProdInfo> availableProds) {
        this.availableProds = availableProds;
    }

    public Vector <String> getAprvFriends() {
        return aprvFriends;
    }

    public void setAprvFriends(Vector <String> aprvFriends) {
        this.aprvFriends = aprvFriends;
    }

    public Vector <ProdInfo> getCompletedProds() {
        return completedProds;
    }

    public void setCompletedProds(Vector <ProdInfo> completedProds) {
        this.completedProds = completedProds;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getAddNewItem() {
        return addNewItem;
    }

    public void setAddNewItem(String addNewItem) {
        this.addNewItem = addNewItem;
    }

    public String getRemoveItem() {
        return RemoveItem;
    }

    public void setRemoveItem(String RemoveItem) {
        this.RemoveItem = RemoveItem;
    }

    public Vector <String> getCompletedContributions() {
        return completedContributions;
    }

    public void setCompletedContributions(Vector <String> completedContributions) {
        this.completedContributions = completedContributions;
    }
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class ProdInfo {
    private String type, reptype; // type of streams
    private String result; // welcome back - wrong user name or pw
    private String name,desc, img;
    private int price, qty, paid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReptype() {
        return reptype;
    }

    public void setReptype(String reptype) {
        this.reptype = reptype;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
/*
type of msg are
(key is type)
log
reg


type of reply 
(key is reptype)
replog
repreg


type of result
(key is reptype)
fail
success


*/