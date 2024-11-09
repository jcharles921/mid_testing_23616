document.addEventListener("DOMContentLoaded", function () {
  fetchBooks();
  fetchBorrowedBooks();
  const tabs = document.querySelectorAll(".tab-link");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach((tab) => {
    tab.addEventListener("click", (event) => {
      event.preventDefault();

      // Remove 'active' class from all tabs and contents
      tabs.forEach((t) => t.classList.remove("active"));
      contents.forEach((c) => c.classList.remove("active"));

      // Add 'active' class to the clicked tab and its content
      tab.classList.add("active");
      const targetId = tab.getAttribute("data-target");
      document.getElementById(targetId).classList.add("active");
    });
  });
});

function fetchBooks() {
  fetch("books")
    .then((response) => response.json())
    .then((books) => {
      const tbody = document.getElementById("books-table-body");

      if (!books || books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No books available</td></tr>';
        return;
      }
      fetchBorrowedBooks();
      tbody.innerHTML = books
        .map((book) => {
          // Safely access nested properties
          const roomCode = book?.shelf?.room?.roomCode || "Unassigned";
          const bookCategory = book?.shelf?.bookCategory || "Uncategorized";
          const publisherName = book?.publisherName || "Unknown";
          const bookStatus = book?.bookStatus || "Unknown";

          return `
            <tr>
              <td>${book?.title || "Untitled"}</td>
              <td>${bookCategory}</td>
              <td>${roomCode}</td>
              <td>${publisherName}</td>
              <td>${bookStatus}</td>
              <td>
                <button onclick="deleteBook('${book?.bookId}')" 
                        class="action-btn delete-btn">Delete</button>
              </td>
            </tr>
          `;
        })
        .join("");
    })
    .catch((error) => {
      console.error("Error fetching books:", error);
      document.getElementById("books-table-body").innerHTML =
        '<tr><td colspan="6">Error loading books</td></tr>';
    });
}
function fetchBorrowedBooks() {
  fetch("borrowBook", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "same-origin",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch borrowed books");
      }
      return response.json();
    })
    .then((borrowedBooks) => {
      const tbody = document.getElementById("borrows-table-body");
      tbody.innerHTML = ""; // Clear existing rows

      if (!borrowedBooks || borrowedBooks.length === 0) {
        const noDataRow = document.createElement("tr");
        const noDataCell = document.createElement("td");
        noDataCell.setAttribute("colspan", "7");
        noDataCell.textContent = "No borrowed books found";
        noDataRow.appendChild(noDataCell);
        tbody.appendChild(noDataRow);
        return;
      }

      borrowedBooks.forEach((borrow) => {
        const row = document.createElement("tr");

        // Book Title
        const titleCell = document.createElement("td");
        titleCell.textContent = borrow.book.title;
        row.appendChild(titleCell);

        // Reader Name
        const readerCell = document.createElement("td");
        readerCell.textContent =
          borrow.reader.firstName + " " + borrow.reader.lastName;
        row.appendChild(readerCell);

        // Pickup Date
        const pickupCell = document.createElement("td");
        pickupCell.textContent = new Date(
          borrow.pickupDate
        ).toLocaleDateString();
        row.appendChild(pickupCell);

        // Due Date
        const dueCell = document.createElement("td");
        dueCell.textContent = new Date(borrow.dueDate).toLocaleDateString();
        row.appendChild(dueCell);

        // Return Date
        const returnCell = document.createElement("td");
        returnCell.textContent = borrow.returnDate
          ? new Date(borrow.returnDate).toLocaleDateString()
          : "Not returned";
        row.appendChild(returnCell);

        // Fine
        const fineCell = document.createElement("td");
        fineCell.textContent = borrow.fine + " Rwf";
        row.appendChild(fineCell);

        // Late Charges
        const lateChargesCell = document.createElement("td");
        lateChargesCell.textContent = borrow.lateChargeFees + "  Rwf";
        row.appendChild(lateChargesCell);

        tbody.appendChild(row);
      });
    })
    .catch((error) => {
      console.error("Error:", error);
      const tbody = document.getElementById("borrows-table-body");
      tbody.innerHTML = ""; // Clear existing rows
      const errorRow = document.createElement("tr");
      const errorCell = document.createElement("td");
      errorCell.setAttribute("colspan", "7");
      errorCell.textContent = "Error loading borrowed books";
      errorRow.appendChild(errorCell);
      tbody.appendChild(errorRow);
    });
}
function fetchMembershipRequests() {
  fetch(`membership/all?role=LIBRARIAN`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Failed to fetch membership requests.");
      }
      return response.json();
    })
    .then((memberships) => {
      const tbody = document.getElementById("membership-table-body");
      tbody.innerHTML = ""; // Clear existing content

      if (!memberships || memberships.length === 0) {
        const noDataRow = document.createElement("tr");
        const noDataCell = document.createElement("td");
        noDataCell.colSpan = 6;
        noDataCell.textContent = "No membership requests found";
        noDataRow.appendChild(noDataCell);
        tbody.appendChild(noDataRow);
        return;
      }

      memberships.forEach((membership) => {
        const row = document.createElement("tr");

        // Name cell
        const nameCell = document.createElement("td");
        nameCell.textContent = `${membership?.reader?.firstName || "Unknown"} ${
          membership?.reader?.lastName || ""
        }`;
        row.appendChild(nameCell);

        // Membership Type cell
        const typeCell = document.createElement("td");
        typeCell.textContent =
          membership?.membershipType?.membershipName || "Unknown";
        row.appendChild(typeCell);

        // Registration Date cell
        const regDateCell = document.createElement("td");
        regDateCell.textContent = new Date(
          membership?.registrationDate
        ).toLocaleDateString();
        row.appendChild(regDateCell);

        // Expiration Date cell
        const expDateCell = document.createElement("td");
        expDateCell.textContent = new Date(
          membership?.expiringTime
        ).toLocaleDateString();
        row.appendChild(expDateCell);

        // Status cell
        const statusCell = document.createElement("td");
        statusCell.textContent = membership?.membershipStatus || "Unknown";
        row.appendChild(statusCell);

        // Actions cell
        const actionsCell = document.createElement("td");
        if (membership?.membershipStatus === "PENDING") {
          const acceptButton = document.createElement("button");
          acceptButton.textContent = "Accept";
          acceptButton.className = "action-btn accept-btn";
          acceptButton.onclick = () =>
            handleMembershipRequest(membership?.membershipId, "accept");

          const refuseButton = document.createElement("button");
          refuseButton.textContent = "Refuse";
          refuseButton.className = "action-btn refuse-btn";
          refuseButton.onclick = () =>
            handleMembershipRequest(membership?.membershipId, "refuse");

          actionsCell.appendChild(acceptButton);
          actionsCell.appendChild(refuseButton);
        } else {
          actionsCell.textContent = "No actions available";
        }
        row.appendChild(actionsCell);

        tbody.appendChild(row);
      });
    })
    .catch((error) => {
      console.error("Error fetching membership requests:", error);
      const tbody = document.getElementById("membership-table-body");
      tbody.innerHTML =
        '<tr><td colspan="6">Error loading membership requests</td></tr>';
    });
}
