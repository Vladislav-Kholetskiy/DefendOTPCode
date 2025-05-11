    package SpecialTools.model;

    public class User {
        private int id;
        private String login;
        private String password; // хэш
        private String role;     // "ADMIN" или "USER"

        public User() {}

        public User(int id, String login, String password, String role) {
            this.id = id;
            this.login = login;
            this.password = password;
            this.role = role;
        }

        // геттеры и сеттеры
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
