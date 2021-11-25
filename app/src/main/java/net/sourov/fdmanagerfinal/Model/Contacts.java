package net.sourov.fdmanagerfinal.Model;

public class Contacts {

    String name,number,dateOfBirth,address,imageUrl,uniqueID,fd_added_date;

    public Contacts() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getFd_added_date() {
        return fd_added_date;
    }

}
