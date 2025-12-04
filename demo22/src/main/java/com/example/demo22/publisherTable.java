package com.example.demo22;

import java.util.Date;

public class publisherTable {

    public static class Publisher{
        private int id;
        private String name;
        private String country;
        private Date establishedDate;
        private String website;
        private String email;
        private int size;
        private String specialization;
        private boolean isActive;
        private int gamesPublished;
        private double share;
        private float budget;

        Publisher(int id, String name, String country, Date establishedDate, String website,
                  String email, int size, String specialization, boolean isActive, int gamesPublished,
                  double share, float budget) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.establishedDate = establishedDate;
            this.website = website;
            this.email = email;
            this.size = size;
            this.specialization = specialization;
            this.isActive = isActive;
            this.gamesPublished = gamesPublished;
            this.share = share;
            this.budget = budget;
        }

        public int getId(){
            return id;
        }
        public String getName(){
            return name;
        }
        public String getCountry(){
            return country;
        }
        public Date getEstablishedDate(){
            return establishedDate;
        }
        public String getWebsite(){
            return website;
        }
        public String getEmail(){
            return email;
        }
        public int getSize(){
            return size;
        }
        public String getSpecialization(){
            return specialization;
        }
        public boolean getIsActive(){
            return isActive;
        }
        public int getGamesPublished(){
            return gamesPublished;
        }
        public double getShare(){
            return share;
        }
        public float getBudget(){
            return budget;
        }
    }


}
