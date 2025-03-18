DROP DATABASE IF EXISTS ChatDB;
GO

CREATE DATABASE ChatDB;
GO

USE ChatDB;
GO

CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL 
);
GO

CREATE TABLE Messages (
    message_id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL, 
    receiver_id INT NOT NULL, 
    content NVARCHAR(MAX) NOT NULL, 
    timestamp DATETIME DEFAULT GETDATE(), 
    FOREIGN KEY (sender_id) REFERENCES Users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id)
);
GO

INSERT INTO Users (username, password) 
VALUES 
('admin', '123'),
('user1', '123'),
('user2', '123');
GO

INSERT INTO Messages (sender_id, receiver_id, content) 
VALUES 
(1, 2, 'Hello user1!'),
(2, 1, 'Hi admin, how are you?');
GO

SELECT * FROM Users
DELETE FROM Users WHERE user_id = '5';
Select * from Users