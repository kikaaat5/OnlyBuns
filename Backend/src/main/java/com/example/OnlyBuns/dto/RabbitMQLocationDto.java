package com.example.OnlyBuns.dto;

public class RabbitMQLocationDto {
        private int id;
        private String name;
        private String location;
       // private String identifier;

        // Getteri i setteri
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        @Override
        public String toString() {
            return "ID: " + id + ", Naziv: " + name + ", Lokacija: " + location;
        }

}
