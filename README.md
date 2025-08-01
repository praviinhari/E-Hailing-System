# E-Hailing System 🚗💳

This is a Java-based E-Hailing System developed as a group assignment for the **DIT2274 Object Oriented Programming** course at UCSI College (Trimester: January 2025). The project simulates a basic ride-hailing application with roles for customers, drivers, and administrators. It features a login system, ride booking, payment processing, and feedback handling, all connected to a Microsoft Access database.

---

## 📁 Project Structure

- **Java Classes (OOP)**
  - `User`, `Driver`, `Passenger`, `Admin`, etc.
  - `RideService` interface and `Payment` abstract class
  - Subclasses like `CardPayment`, `WalletPayment`, `CustomerService`, `Notification`, `Review`, `Vehicle`

- **GUI**
  - Role-based Login and Sign-up forms
  - Ride selection interface
  - Payment options (Card & E-wallet)
  - Review and support message pages
  - Admin panel to manage complaints and feedback

- **Database**
  - Integrated with **Microsoft Access (.accdb)**
  - Tables include:
    - `Users`, `Bookings`, `Payments`, `Reviews`, `SupportMessages`, `Drivers`

- **UML & ERD Diagrams**
  - Complete class relationships and database entity designs included.

---

## 🧑‍💻 Roles & Functionalities

### 👤 **Customer**
- Sign up / Login
- Book rides by choosing pickup and drop-off
- Choose payment method: Card or E-wallet
- Submit reviews and complaints
- View booking history

### 🚖 **Driver**
- Sign up / Login
- Input vehicle information
- Get assigned bookings

### 👨‍💼 **Admin**
- View and manage all user feedback
- View support messages and take action
- Manage system data

---

## 💻 Technologies Used

- **Java (OOP & Swing)** for core logic and GUI
- **Microsoft Access (.accdb)** for backend database
- **JDBC (UCanAccess)** for DB connectivity
- **UML & ERD Diagrams** for system modeling

---

## 🔐 Login Roles (Demo)

| Role    | Sample Credentials |
|---------|--------------------|
| Customer | userID: `C001`, pass: `1234` |
| Driver   | userID: `D001`, pass: `abcd` |
| Admin    | userID: `A001`, pass: `admin` |

*(Note: Update with actual demo credentials if needed.)*

---

## 📸 Screenshots & UI Flow

The GUI demonstrates a full end-to-end flow:

1. **Login / Register** with role selection
2. **Booking** a ride with pickup & drop-off
3. **Payment** using Card or E-wallet
4. **Feedback** submission (Review + Support)
5. **Admin Panel** to manage system operations

(Refer to `/screenshots/` or `OOP ASSIGNMENT PART 2.docx` for images)

---

## 📊 Diagrams

- **UML Class Diagram** (See: `UML Diagram.docx`)
- **ERD Diagram** (See: `OOP ASSIGNMENT PART 2.docx`)

---

## 🧑‍🤝‍🧑 Contributors

| Name                  | ID           | Portfolio |
|-----------------------|--------------|-----------|
| Muhammad Farhan bin Mokhtar | 20023000331 | [Farhan's CN](https://www.thecn.com/MM4290/section/showcase/6818bca1c174a0148908d4dc) |
| Praviin a/l Hari      | 2023000239   | [Praviin's CN](https://www.thecn.com/PH643/section/showcase/6818ca4ab147cb80a00a3bd1) |
| Kannan Kumaaran       | 2023000447   | [Kannan's CN](https://www.thecn.com/KR1222/section/showcase/6818bd8f6aadc0086403db82) |
| Keashendhaaran a/l Prem Kumar | 2022000192 | [Kaeshendharaan's CN](https://www.thecn.com/KA1152/section/showcase/6818b9fe4a5f53796f09aa24) |

---

## 📦 How to Run

1. Clone the repo:
   ```bash
   git clone https://github.com/yourusername/e-hailing-system.git
