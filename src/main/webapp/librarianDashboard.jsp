<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <% String userName = (String)
session.getAttribute("userName"); String role = (String)
session.getAttribute("role"); %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Librarian Dashboard</title>
    <link rel="stylesheet" href="./styles/librarian.css" />
    <style>
      /* Drawer Styles */
      .drawer {
        position: fixed;
        top: 0;
        right: 0;
        width: 300px;
        height: 100%;
        background-color: #f8f8f8;
        box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
        padding: 20px;
        display: flex;
        flex-direction: column;
        justify-content: start;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        z-index: 1000;
      }

      .drawer-content {
        margin-top: 20px;
      }

      .hidden {
        display: none;
      }

      .drawer.open {
        transform: translateX(0);
      }

      .close-btn {
        background: none;
        border: none;
        font-size: 24px;
        align-self: flex-end;
        cursor: pointer;
      }

      #add-book-form {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      #add-book-form input,
      #add-book-form button {
        padding: 8px;
        border-radius: 4px;
      }

      #add-book-form button {
        background-color: #77ba99;
        color: white;
        border: none;
        cursor: pointer;
      }

      .drawer-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: none;
        z-index: 999;
      }

      .drawer-overlay.visible {
        display: block;
      }
    </style>
  </head>
  <body>
    <div class="dashboard">
      <!-- Sidebar -->
      <aside class="sidebar">
        <h2>Library Admin</h2>
        <nav>
          <ul>
            <li><a href="#book-management">Book Management</a></li>
            <li><a href="#membership-management">Membership Management</a></li>
          </ul>
        </nav>
      </aside>

      <!-- Main Panel -->
      <main class="main-panel">
        <header>
          <h1>Welcome, <%= userName %>!</h1>
          <p>Role: <%= role %></p>
        </header>

        <!-- Books Section -->
        <div class="card">
          <div class="section-header">
            <h3>Available Books</h3>
            <button onclick="openAddBookDrawer()" class="add-book-btn">
              Add New Book
            </button>
          </div>
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Edition</th>
                <th>ISBN</th>
                <th>Publisher</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="books-table-body">
              <tr>
                <td colspan="6">Loading books...</td>
              </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>

    <!-- Add Book Drawer -->
    <div class="drawer-overlay" id="drawer-overlay"></div>
    <div id="add-book-drawer" class="drawer hidden">
      <div class="drawer-content">
        <button class="close-btn" onclick="closeDrawer()">×</button>
        <h3>Add New Book</h3>
        <form id="add-book-form">
          <input
            type="text"
            id="title"
            name="title"
            placeholder="Book Title"
            required
          />
          <input
            type="number"
            id="edition"
            name="edition"
            placeholder="Edition"
            required
          />
          <input
            type="text"
            id="ISBNCode"
            name="ISBNCode"
            placeholder="ISBN Code"
            required
          />
          <input
            type="text"
            id="publisherName"
            name="publisherName"
            placeholder="Publisher Name"
            required
          />
          <input
            type="date"
            id="publicationYear"
            name="publicationYear"
            required
          />
          <button type="submit">Add Book</button>
        </form>
      </div>
    </div>

    <script>
      // Initial data and role
      const userRole = "<%= role %>";

      // Fetch and display books
      function fetchBooks() {
        fetch("books")
          .then((response) => response.json())
          .then((books) => {
            const tbody = document.getElementById("books-table-body");
            console.log(books);
            if (!books || books.length === 0) {
              tbody.innerHTML =
                '<tr><td colspan="6">No books available</td></tr>';
              return;
            }

            tbody.innerHTML = books
              ?.map(
                (book) => `
                        <tr>
                            <td>${book.title}</td>
                            <td>${book.edition}</td>
                            <td>${book.ISBNCode}</td>
                            <td>${book.publisherName}</td>
                            <td>${book.bookStatus}</td>
                            <td>
                                <button onclick="deleteBook('${book.bookId}')" class="action-btn delete-btn">Delete</button>
                            </td>
                        </tr>
                    `
              )
              .join("");
          })
          .catch((error) => {
            console.error("Error fetching books:", error);
            document.getElementById("books-table-body").innerHTML =
              '<tr><td colspan="6">Error loading books</td></tr>';
          });
      }

      // Add book function
      function addBook(bookData) {
        fetch("books", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            ...bookData,
            role: userRole,
          }),
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.error) {
              alert(data.error);
            } else {
              alert("Book added successfully");
              closeDrawer();
              fetchBooks();
            }
          })
          .catch((error) => {
            console.error("Error adding book:", error);
            alert("Error adding book");
          });
      }

      // Delete book function
      function deleteBook(bookId) {
        if (!confirm("Are you sure you want to delete this book?")) return;

        fetch("books", {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            bookId: bookId,
            role: userRole,
          }),
        })
          .then((response) => response.json())
          .then((data) => {
            if (data.error) {
              alert(data.error);
            } else {
              alert("Book deleted successfully");
              fetchBooks();
            }
          })
          .catch((error) => {
            console.error("Error deleting book:", error);
            alert("Error deleting book");
          });
      }

      // Drawer functions
      function openAddBookDrawer() {
        const overlay = document.getElementById("drawer-overlay");
        const drawer = document.getElementById("add-book-drawer");
        overlay.classList.add("visible");
        drawer.classList.remove("hidden");
        drawer.classList.add("open");
      }

      function closeDrawer() {
        const overlay = document.getElementById("drawer-overlay");
        const drawer = document.getElementById("add-book-drawer");
        overlay.classList.remove("visible");
        drawer.classList.remove("open");
        setTimeout(() => drawer.classList.add("hidden"), 300);
      }

      // Event Listeners
      document.addEventListener("DOMContentLoaded", () => {
        const overlay = document.getElementById("drawer-overlay");
        const form = document.getElementById("add-book-form");

        overlay.addEventListener("click", closeDrawer);

        form.addEventListener("submit", (event) => {
          event.preventDefault();
          const formData = {
            title: form.title.value,
            edition: form.edition.value,
            ISBNCode: form.ISBNCode.value,
            publisherName: form.publisherName.value,
            publicationYear: form.publicationYear.value,
          };
          addBook(formData);
        });

        // Initial books fetch
        fetchBooks();
      });
    </script>
  </body>
</html>
