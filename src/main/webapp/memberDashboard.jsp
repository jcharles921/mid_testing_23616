<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Get session attributes for user validation
    String userName = (String) session.getAttribute("userName");
    String role = (String) session.getAttribute("role");

    // Validate that the role is "MEMBER" (Students or Teachers)
    if (session == null || userName == null || role == null ) {
        response.sendRedirect("index.html");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Member Dashboard</title>
    <link rel="stylesheet" href="./styles/member.css" />
  </head>
  <body>
    <div class="dashboard">
      <!-- Sidebar -->
      <aside class="sidebar">
        <h2>Member Dashboard</h2>
        <nav>
          <ul>
            <li><a href="#membership-status">My Membership</a></li>
            <li><a href="#borrowed-books">Borrowed Books</a></li>
            <li><a href="#fines-payments">Fines & Payments</a></li>
          </ul>
        </nav>
      </aside>

      <!-- Main Panel -->
      <main class="main-panel">
        <header>
          <h1>Welcome, <%= userName %>!</h1>
        </header>

        <section class="summary-cards">
          <!-- Membership Status -->
          <div class="card" id="membership-status">
            <h3>Membership Details</h3>
            <p><strong>Type:</strong> Gold</p> <!-- Replace with dynamic data -->
            <p><strong>Expiration:</strong> 12/31/2024</p> <!-- Replace with dynamic data -->
            <p><strong>Borrowing Limit:</strong> 10 books</p> <!-- Replace with dynamic data -->
          </div>

          <!-- Borrowed Books -->
          <div class="card" id="borrowed-books">
            <h3>Borrowed Books</h3>
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Due Date</th>
                  <th>Fine</th>
                </tr>
              </thead>
              <tbody>
                <!-- Example Data (Replace with dynamic values) -->
                <tr>
                  <td>The Great Gatsby</td>
                  <td>11/15/2024</td>
                  <td>$0</td>
                </tr>
                <tr>
                  <td>1984</td>
                  <td>11/20/2024</td>
                  <td>$2</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Fines and Payments -->
          <div class="card" id="fines-payments">
            <h3>Fines & Payments</h3>
            <p><strong>Outstanding Fines:</strong> $2</p> <!-- Replace dynamically -->
            <p><strong>Payment History:</strong></p>
            <ul>
              <!-- Example Payment History -->
              <li>Paid $5 on 10/01/2024</li>
              <li>Paid $3 on 09/15/2024</li>
            </ul>
          </div>

          <!-- Search Books -->
          <div class="card">
            <h3>Search Books</h3>
            <input
              type="text"
              placeholder="Search by title or category..."
              class="search-bar"
            />
          </div>
        </section>
      </main>
    </div>
  </body>
</html>
