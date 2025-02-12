// Initial data and role
const userRole = "LIBRARIAN";
const tbody = document.getElementById("membership-table-body");
// Tab functionality
function showTab(tabId) {
  // Hide all tab contents
  document.querySelectorAll(".tab-content").forEach((tab) => {
    tab.classList.remove("active");
  });

  // Show selected tab content
  document.getElementById(tabId).classList.add("active");

  // If showing membership tab, fetch membership data
  if (tabId === "membership-management") {
    fetchMembershipRequests();
  }
}

// Fetch and display books
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

// Fetch and display membership requests
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

// Handle membership request (accept/refuse)
function handleMembershipRequest(requestId, action) {
  fetch("membership", {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      requestId,
      action,
      role: userRole,
    }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert(`Membership request ${action}ed successfully`);
        fetchMembershipRequests();
      }
    })
    .catch((error) => {
      console.error(`Error ${action}ing membership request:`, error);
      alert(`Error ${action}ing membership request`);
    });
}

// Existing functions
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
      shelfId: form.shelfId.value,
      role: "LIBRARIAN",
    };
    addBook(formData);
  });

  // Initial books fetch
  fetchBooks();
});
// Fetch and display shelves
function fetchShelves() {
  fetch("shelves")
    .then((response) => response.json())
    .then((shelves) => {
      const tbody = document.getElementById("shelf-table-body");
      if (!shelves || shelves.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No shelves available</td></tr>';
        return;
      }

      tbody.innerHTML = shelves
        .map(
          (shelf) => `
    <tr>
      <td>${shelf?.bookCategory}</td>
      <td>${shelf?.room?.roomCode || "Unassigned"}</td>
      <td>${shelf?.availableStock}</td>
      <td>
        <button onclick="openAssignRoomDrawer('${
          shelf?.shelfId
        }')" class="action-btn accept-btn">Assign Room</button>
        <button onclick="deleteShelf('${
          shelf?.shelfId
        }')" class="action-btn delete-btn">Delete</button>
      </td>
    </tr>
  `
        )
        .join("");
      const shelfSelect = document.getElementById("shelfId");
      shelfSelect.innerHTML =
        '<option value="">Select a shelf category</option>' +
        shelves
          .map(
            (shelf) => `
                <option value="${shelf.shelfId}">
                    ${shelf.bookCategory} (Available: ${shelf.availableStock})
                </option>
            `
          )
          .join("");
    })
    .catch((error) => {
      console.error("Error fetching shelves:", error);
      document.getElementById("shelf-table-body").innerHTML =
        '<tr><td colspan="3">Error loading shelves</td></tr>';
    });
}

// Open assign room drawer
function openAssignRoomDrawer(shelfId) {
  document.getElementById("assign-room-form").onsubmit = (event) => {
    event.preventDefault();
    assignRoomToShelf(shelfId, event.target.roomId.value);
  };

  populateRoomSelect();
  openDrawer("assign-room-drawer");
}

// Populate room select options
// Fixed populateRoomSelect function
function populateRoomSelect() {
  fetch("rooms")
    .then((response) => response.json())
    .then((rooms) => {
      // First check if rooms exists and is an array
      if (!Array.isArray(rooms)) {
        console.error("Expected rooms to be an array, got:", rooms);
        return;
      }

      const select = document.getElementById("roomSelect");
      const roomSelect = document.getElementById("roomId");

      // Check if both select elements exist
      if (!select || !roomSelect) {
        console.error("Could not find one or both select elements");
        return;
      }

      // Clear existing options first
      select.innerHTML = '<option value="">Select a room</option>';
      roomSelect.innerHTML = '<option value="">Select a room</option>';

      // Add new options
      rooms.forEach((room) => {
        // Verify room object has required properties
        if (!room || !room.roomId || !room.roomCode) {
          console.warn("Invalid room object:", room);
          return;
        }

        // Add option to first select
        select.innerHTML += `
          <option value="${room.roomId}">
            ${room.roomCode}
          </option>
        `;

        // Add option to second select
        roomSelect.innerHTML += `
          <option value="${room.roomId}">
            ${room.roomCode}
          </option>
        `;
      });
    })
    .catch((error) => {
      console.error("Error fetching rooms:", error);
    });
}

// Assign room to shelf
function assignRoomToShelf(shelfId, roomId) {
  fetch("rooms", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ shelfId, roomId }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Room assigned successfully");
        closeDrawer();
        fetchShelves();
      }
    })
    .catch((error) => {
      console.error("Error assigning room:", error);
    });
}

// Open add room drawer
function openAddRoomDrawer() {
  document.getElementById("add-room-form").onsubmit = (event) => {
    event.preventDefault();
    const formData = {
      roomCode: event.target.roomName.value,
    };
    addRoom(formData);
  };

  openDrawer("add-room-drawer");
  populateRoomSelect();
}

// Add room
function addRoom(roomData) {
  fetch("rooms", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(roomData),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Room added successfully");
        closeDrawer();
      }
    })
    .catch((error) => {
      console.error("Error adding room:", error);
    });
}

// Utility to open a specific drawer
function openDrawer(drawerId) {
  document.getElementById("drawer-overlay").classList.add("visible");
  document.getElementById(drawerId).classList.remove("hidden");
  document.getElementById(drawerId).classList.add("open");
}

// Close drawer
function closeDrawer() {
  document.getElementById("drawer-overlay").classList.remove("visible");
  document.querySelectorAll(".drawer").forEach((drawer) => {
    drawer.classList.remove("open");
    setTimeout(() => drawer.classList.add("hidden"), 300);
  });
}
let currentRoomId = null; // Track the room being edited
let shelves = []; // Store shelves temporarily

function openAddShelfDrawer() {
  openDrawer("add-shelf-drawer");
  fetchRooms();
}

document
  .getElementById("add-shelf-form")
  .addEventListener("submit", (event) => {
    event.preventDefault();

    const shelfData = {
      // shelfName: event.target.shelfName.value,
      bookCategory: event.target.bookCategory.value,
      initialStock: parseInt(event.target.initialStock.value, 10),
      borrowedNumber: parseInt(event.target.borrowedNumber.value, 10),
      availableStock: parseInt(event.target.availableStock.value, 10),
      room: {
        roomId: event.target.roomId.value,
      },
    };

    addShelf(shelfData);
  });

function addShelf(shelfData) {
  fetch("shelves", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(shelfData),
  })
    .then((response) => response.json())
    .then((shelf) => {
      if (shelf.error) {
        alert(shelf.error);
        return;
      }

      shelves.push(shelf);
      updateShelfList();
      closeDrawer();
    })
    .catch((error) => {
      console.error("Error adding shelf:", error);
    });
}

function updateShelfList() {
  const shelfList = document.getElementById("shelf-table-body");
  if (shelves.length === 0) {
    shelfList.innerHTML = "<li>No shelves added yet</li>";
  } else {
    shelfList.innerHTML = shelves
      .map(
        (shelf) => `
    <tr>
      <td>${shelf?.bookCategory}</td>
      <td>${shelf?.room?.roomCode || "Unassigned"}</td>
      <td>${shelf?.availableStock}</td>
      <td>
        <button onclick="openAssignRoomDrawer('${
          shelf?.shelfId
        }')" class="action-btn accept-btn">Assign Room</button>
        <button onclick="deleteShelf('${
          shelf?.shelfId
        }')" class="action-btn delete-btn">Delete</button>
      </td>
    </tr>
  `
      )
      .join("");
  }
}

function deleteShelf(shelfId) {
  if (!confirm("Are you sure you want to delete this shelf?")) return;

  fetch(`shelves?shelfId=${shelfId}`, { method: "DELETE" })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
        return;
      }

      shelves = shelves.filter((shelf) => shelf.id !== shelfId);
      updateShelfList();
    })
    .catch((error) => {
      console.error("Error deleting shelf:", error);
    });
}

function openEditRoomDrawer(roomId, roomName, roomCapacity) {
  currentRoomId = roomId;
  shelves = []; // Reset shelves when editing
  fetchShelvesForRoom(roomId);

  document.getElementById("drawer-title").textContent = "Edit Room";
  document.getElementById("roomName").value = roomName;
  document.getElementById("roomCapacity").value = roomCapacity;

  const form = document.getElementById("add-room-form");
  form.onsubmit = (event) => {
    event.preventDefault();
    const formData = {
      id: roomId,
      roomName: form.roomName.value,
      roomCapacity: form.roomCapacity.value,
    };
    updateRoom(formData);
  };

  openDrawer("add-room-drawer");
}

function fetchShelvesForRoom(roomId) {
  fetch(`shelves?roomId=${roomId}`)
    .then((response) => response.json())
    .then((data) => {
      shelves = data;
      updateShelfList();
    })
    .catch((error) => {
      console.error("Error fetching shelves:", error);
    });
}
function fetchRooms() {
  fetch("rooms")
    .then((response) => response.json())
    .then((rooms) => {
      const roomSelect = document.getElementById("roomId");
      console.log(rooms);
      rooms.forEach((room) => {
        const option = document.createElement("option");
        option.value = room.roomId;
        option.textContent = `${room.roomCode}`;
        roomSelect.appendChild(option);
      });
    })
    .catch((error) => console.error("Failed to fetch rooms:", error));
}
function deleteShelf(shelfId) {
  if (!confirm("Are you sure you want to delete this shelf?")) return;

  fetch(`shelves?shelfId=${shelfId}`, { method: "DELETE" })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        alert(data.error);
      } else {
        alert("Shelf deleted successfully");
        fetchShelves(); // Refresh the shelf list after deletion
      }
    })
    .catch((error) => {
      console.error("Error deleting shelf:", error);
      alert("Error deleting shelf");
    });
}

// Fetch shelves on page load
document.addEventListener("DOMContentLoaded", fetchShelves);
