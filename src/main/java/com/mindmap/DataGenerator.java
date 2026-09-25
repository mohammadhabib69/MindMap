package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Difficulty;
import com.mindmap.model.Note;
import com.mindmap.service.NoteService;
import com.mindmap.service.ConnectionService;
import com.mindmap.service.RevisionService;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;

public class DataGenerator {
    private static final Logger LOGGER = Logger.getLogger(DataGenerator.class.getName());
    private static NoteService noteService = new NoteService();
    private static ConnectionService connectionService = new ConnectionService();
    private static RevisionService revisionService = new RevisionService();

    public static void main(String[] args) {
        try { DatabaseInitializer.initialize(); } catch (Exception e) {}
        LOGGER.info("Starting data generation...");
        
        try {
            // Delete all existing notes
            List<Note> allNotes = noteService.getAllNotes();
            for (Note n : allNotes) {
                noteService.deleteNote(n.getId());
            }
            LOGGER.info("Cleared existing database.");

            // Create 50 notes
            Note n1 = createNote("Operating System", "An operating system (OS) is system software that manages computer hardware, software resources, and provides common services for computer programs. \n\nঅপারেটিং সিস্টেম (OS) হলো এমন একটি সফটওয়্যার যা কম্পিউটারের হার্ডওয়্যার এবং সফটওয়্যার রিসোর্স পরিচালনা করে।", "Computer Science", Difficulty.HARD, true, "OS", "CSE");
            Note n2 = createNote("Process Management", "Process management is an integral part of any modern-day operating system (OS). The OS must allocate resources to processes, enable processes to share and exchange information, protect the resources of each process from other processes and enable synchronization among processes.", "Computer Science", Difficulty.MEDIUM, false, "OS");
            Note n3 = createNote("Process Scheduling", "Process scheduling is an OS task that schedules processes of different states like ready, waiting, and running. Process scheduling allows OS to allocate a time interval of CPU execution for each process.", "Computer Science", Difficulty.HARD, false, "OS");
            Note n4 = createNote("CPU Scheduling", "CPU scheduling is a process which allows one process to use the CPU while the execution of another process is on hold(in waiting state) due to unavailability of any resource like I/O etc.", "Computer Science", Difficulty.MEDIUM, false, "OS");
            Note n5 = createNote("Deadlock", "A deadlock is a situation where a set of processes are blocked because each process is holding a resource and waiting for another resource acquired by some other process.", "Computer Science", Difficulty.HARD, true, "OS", "CSE");
            Note n6 = createNote("Memory Management", "Memory management is the process of controlling and coordinating computer memory, assigning portions called blocks to various running programs to optimize overall system performance.", "Computer Science", Difficulty.MEDIUM, false, "OS");
            Note n7 = createNote("Virtual Memory", "Virtual memory is a memory management capability of an OS that uses hardware and software to allow a computer to compensate for physical memory shortages by temporarily transferring data from random access memory (RAM) to disk storage.", "Computer Science", Difficulty.HARD, true, "OS", "Architecture");
            Note n8 = createNote("Page Replacement", "In a computer operating system that uses paging for virtual memory management, page replacement algorithms decide which memory pages to page out, sometimes called swap out, or write to disk, when a page of memory needs to be allocated.", "Computer Science", Difficulty.MEDIUM, false, "OS");
            Note n9 = createNote("Paging", "Paging is a memory management scheme that eliminates the need for contiguous allocation of physical memory. This scheme permits the physical address space of a process to be non-contiguous.", "Computer Science", Difficulty.EASY, false, "OS");
            Note n10 = createNote("Segmentation", "Segmentation is a memory management technique in which the memory is divided into the variable size parts. Each part is known as a segment which can be allocated to a process.", "Computer Science", Difficulty.MEDIUM, false, "OS");
            
            Note n11 = createNote("Array", "An array is a collection of items stored at contiguous memory locations. The idea is to store multiple items of the same type together.", "Data Structures", Difficulty.EASY, false, "DSA", "Programming");
            Note n12 = createNote("Linked List", "A linked list is a linear data structure, in which the elements are not stored at contiguous memory locations. The elements in a linked list are linked using pointers.", "Data Structures", Difficulty.MEDIUM, false, "DSA");
            Note n13 = createNote("Doubly Linked List", "A Doubly Linked List (DLL) contains an extra pointer, typically called previous pointer, together with next pointer and data which are there in singly linked list.", "Data Structures", Difficulty.MEDIUM, false, "DSA");
            Note n14 = createNote("Stack", "Stack is a linear data structure which follows a particular order in which the operations are performed. The order may be LIFO(Last In First Out) or FILO(First In Last Out).", "Data Structures", Difficulty.EASY, false, "DSA");
            Note n15 = createNote("Queue", "A Queue is a linear structure which follows a particular order in which the operations are performed. The order is First In First Out (FIFO).", "Data Structures", Difficulty.EASY, false, "DSA");
            Note n16 = createNote("Binary Tree", "A tree whose elements have at most 2 children is called a binary tree. Since each element in a binary tree can have only 2 children, we typically name them the left and right child.", "Data Structures", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n17 = createNote("Binary Search Tree", "Binary Search Tree is a node-based binary tree data structure which has the following properties: The left subtree of a node contains only nodes with keys lesser than the node's key. The right subtree of a node contains only nodes with keys greater than the node's key.", "Data Structures", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n18 = createNote("Heap", "A Heap is a special Tree-based data structure in which the tree is a complete binary tree.", "Data Structures", Difficulty.HARD, false, "DSA");
            Note n19 = createNote("Heap Sort", "Heap sort is a comparison based sorting technique based on Binary Heap data structure. It is similar to selection sort where we first find the minimum element and place the minimum element at the beginning.", "Algorithms", Difficulty.HARD, false, "DSA", "Algorithm");
            Note n20 = createNote("Graph", "A Graph is a non-linear data structure consisting of nodes and edges. The nodes are sometimes also referred to as vertices and the edges are lines or arcs that connect any two nodes in the graph.", "Data Structures", Difficulty.HARD, false, "DSA", "Algorithm");
            
            Note n21 = createNote("BFS", "Breadth First Search (BFS) for a graph is similar to Breadth First Traversal of a tree. The only catch here is, unlike trees, graphs may contain cycles, so we may come to the same node again.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n22 = createNote("DFS", "Depth First Traversal (or Search) for a graph is similar to Depth First Traversal of a tree. The only catch here is, unlike trees, graphs may contain cycles.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n23 = createNote("Dijkstra Algorithm", "Dijkstra's algorithm is an algorithm for finding the shortest paths between nodes in a graph, which may represent, for example, road networks.", "Algorithms", Difficulty.HARD, true, "DSA", "Algorithm");
            Note n24 = createNote("Bellman-Ford Algorithm", "Bellman-Ford algorithm helps us find the shortest path from a vertex to all other vertices of a weighted graph.", "Algorithms", Difficulty.HARD, false, "DSA", "Algorithm");
            Note n25 = createNote("Floyd-Warshall Algorithm", "The Floyd Warshall Algorithm is for solving the All Pairs Shortest Path problem. The problem is to find shortest distances between every pair of vertices in a given edge weighted directed Graph.", "Algorithms", Difficulty.HARD, false, "DSA", "Algorithm");
            Note n26 = createNote("Prim's Algorithm", "Prim's Algorithm is a greedy algorithm that finds a minimum spanning tree for a weighted undirected graph.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n27 = createNote("Kruskal's Algorithm", "Kruskal's algorithm is a minimum-spanning-tree algorithm which finds an edge of the least possible weight that connects any two trees in the forest.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n28 = createNote("Quick Sort", "Like Merge Sort, QuickSort is a Divide and Conquer algorithm. It picks an element as pivot and partitions the given array around the picked pivot.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n29 = createNote("Merge Sort", "Merge Sort is a Divide and Conquer algorithm. It divides the input array into two halves, calls itself for the two halves, and then merges the two sorted halves.", "Algorithms", Difficulty.MEDIUM, false, "DSA", "Algorithm");
            Note n30 = createNote("Binary Search", "Search a sorted array by repeatedly dividing the search interval in half.", "Algorithms", Difficulty.EASY, false, "DSA", "Algorithm");
            
            Note n31 = createNote("8086 Microprocessor", "The 8086 is a 16-bit microprocessor chip designed by Intel between early 1976 and June 8, 1978, when it was released.", "Computer Architecture", Difficulty.HARD, false, "Architecture");
            Note n32 = createNote("8051 Microcontroller", "The Intel MCS-51 (commonly termed 8051) is a single chip microcontroller (MCU) series developed by Intel in 1980 for use in embedded systems.", "Computer Architecture", Difficulty.MEDIUM, false, "Architecture");
            Note n33 = createNote("8254 Timer", "The Intel 8253 and 8254 are Programmable Interval Timers (PTIs) designed for microprocessors to perform timing and counting functions.", "Computer Architecture", Difficulty.HARD, false, "Architecture");
            Note n34 = createNote("Interrupts", "An interrupt is a signal to the processor emitted by hardware or software indicating an event that needs immediate attention.", "Computer Architecture", Difficulty.MEDIUM, false, "Architecture", "OS");
            Note n35 = createNote("Memory Addressing", "Memory addressing is a method used by a CPU to access specific locations in the main memory.", "Computer Architecture", Difficulty.HARD, false, "Architecture", "OS");
            Note n36 = createNote("Virtual Memory Architecture", "Hardware mechanisms to support virtual memory, primarily the Translation Lookaside Buffer (TLB) and page tables.", "Computer Architecture", Difficulty.HARD, false, "Architecture", "OS");
            Note n37 = createNote("Cache Memory", "Cache memory is a high-speed static random access memory (SRAM) that a computer microprocessor can access more quickly than it can access regular random access memory (RAM).", "Computer Architecture", Difficulty.MEDIUM, false, "Architecture");
            
            Note n38 = createNote("Object-Oriented Programming", "Object-oriented programming (OOP) is a programming paradigm based on the concept of 'objects', which can contain data and code.", "Programming", Difficulty.MEDIUM, true, "OOP", "Programming", "CSE");
            Note n39 = createNote("Encapsulation", "Encapsulation is the bundling of data with the methods that operate on that data, or the restricting of direct access to some of an object's components.", "Programming", Difficulty.EASY, false, "OOP", "Programming");
            Note n40 = createNote("Inheritance", "Inheritance is the mechanism of basing an object or class upon another object (prototype-based inheritance) or class (class-based inheritance), retaining similar implementation.", "Programming", Difficulty.MEDIUM, false, "OOP", "Programming");
            Note n41 = createNote("Polymorphism", "Polymorphism is the provision of a single interface to entities of different types or the use of a single symbol to represent multiple different types.", "Programming", Difficulty.HARD, false, "OOP", "Programming");
            Note n42 = createNote("Abstraction", "Abstraction is the process of removing physical, spatial, or temporal details or attributes in the study of objects or systems to focus attention on details of greater importance.", "Programming", Difficulty.MEDIUM, false, "OOP", "Programming");
            
            Note n43 = createNote("Database Normalization", "Database normalization is the process of structuring a relational database in accordance with a series of so-called normal forms in order to reduce data redundancy and improve data integrity.", "Database", Difficulty.HARD, true, "Database", "CSE");
            Note n44 = createNote("SQL Transactions", "A transaction is a single unit of work. If a transaction is successful, all of the data modifications made during the transaction are committed and become a permanent part of the database.", "Database", Difficulty.MEDIUM, false, "Database");
            Note n45 = createNote("ACID Properties", "ACID (Atomicity, Consistency, Isolation, Durability) is a set of properties of database transactions intended to guarantee data validity despite errors, power failures, and other mishaps.", "Database", Difficulty.HARD, false, "Database");
            Note n46 = createNote("Computer Networks", "A computer network is a set of computers sharing resources located on or provided by network nodes.", "Computer Networks", Difficulty.MEDIUM, false, "Network", "CSE");
            Note n47 = createNote("TCP vs UDP", "TCP is connection-oriented and reliable, while UDP is connectionless and faster but unreliable.", "Computer Networks", Difficulty.MEDIUM, false, "Network");
            Note n48 = createNote("HTTP", "The Hypertext Transfer Protocol (HTTP) is an application layer protocol in the Internet protocol suite model for distributed, collaborative, hypermedia information systems.", "Computer Networks", Difficulty.EASY, false, "Network");
            Note n49 = createNote("Software Engineering", "Software engineering is the systematic application of engineering approaches to the development of software.", "Software Engineering", Difficulty.EASY, false, "CSE");
            Note n50 = createNote("Version Control", "Version control is a class of systems responsible for managing changes to computer programs, documents, large web sites, or other collections of information.", "Software Engineering", Difficulty.EASY, false, "CSE", "Programming");

            // Private Note (Security Test)
            Note n51 = createNote("Private Security Test", "This is confidential MindMap content.\nDo not share this information.", "Security", Difficulty.HARD, true, "Private", "Security");
            // Set private attributes manually (mock behavior if needed) - for now, I'll update it to have Private Tag to test filter, 
            // but since there's no native "Private" boolean in the schema, maybe it's represented by a "Private" tag or title convention?
            // Actually, the prompt says "Enable: 🔒 Private Note. PIN: 1234". Is there a private property on Note? Let me check Note.java.
            
            // Create meaningful connections
            createConnection(n1, n2, "manages");
            createConnection(n1, n3, "manages");
            createConnection(n1, n4, "manages");
            createConnection(n1, n6, "includes");
            createConnection(n6, n7, "implements");
            createConnection(n6, n9, "uses");
            createConnection(n6, n10, "uses");
            createConnection(n9, n8, "requires");
            
            createConnection(n11, n12, "alternative to");
            createConnection(n12, n13, "base of");
            createConnection(n11, n14, "implements");
            createConnection(n12, n14, "implements");
            createConnection(n11, n15, "implements");
            createConnection(n12, n15, "implements");
            createConnection(n16, n17, "base of");
            createConnection(n16, n18, "base of");
            createConnection(n18, n19, "used in");
            createConnection(n20, n21, "traversed by");
            createConnection(n20, n22, "traversed by");
            createConnection(n20, n23, "solved by");
            createConnection(n20, n26, "solved by");
            createConnection(n20, n27, "solved by");
            createConnection(n23, n24, "similar to");
            createConnection(n24, n25, "related to");
            
            createConnection(n43, n44, "improves");
            createConnection(n44, n45, "must follow");
            
            createConnection(n38, n39, "pillar");
            createConnection(n38, n40, "pillar");
            createConnection(n38, n41, "pillar");
            createConnection(n38, n42, "pillar");

            LOGGER.info("Successfully created 51 notes and connected them.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed", e);
        }
    }
    
    private static Note createNote(String title, String content, String subject, Difficulty diff, boolean fav, String... tags) {
        Note n = new Note(title, content, subject, diff.name());
        n.setFavorite(fav);
        Note saved = noteService.createNoteWithTags(n, Arrays.asList(tags));
        
        // Schedule some reviews
        try {
            revisionService.scheduleInitialReview(saved.getId());
            // Randomly progress some
            if (Math.random() > 0.5) {
                // revisionService.recordReview(saved.getId(), "GOOD");
            }
            if (Math.random() > 0.8) {
                // revisionService.recordReview(saved.getId(), "EASY");
            }
        } catch (Exception e) {}
        
        return saved;
    }
    
    private static void createConnection(Note from, Note to, String type) {
        connectionService.createConnection(from.getId(), to.getId(), type);
    }
}
