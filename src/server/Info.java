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
    private String friendName, prodName;
    private int contrAmount;
    
}
class UserInfo {
    private String type, reptype; // type of streams
    private String result; // welcome back - wrong user name or pw
    private String usrName, pw, email, fname, lname; // attribute of user
    private Vector <ProdInfo> wishList; // itemms of user
    private ContrDetails contribution; // obj containing friendname, prodname, contribution amount
    
    private Vector <String> pendFriends; // Pending friends of user

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