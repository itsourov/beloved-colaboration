package net.sourov.fdmanagerfinal.Model;

public class Users {

    public String dateOfBirth,email,name,imageUrl,number;

    public Users(String dateOfBirth, String email, String name, String imageUrl, String number) {
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.number = number;
    }

    public Users() {
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
