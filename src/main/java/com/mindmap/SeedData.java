package com.mindmap;

import com.mindmap.database.DatabaseInitializer;
import com.mindmap.model.Note;
import com.mindmap.model.Tag;
import com.mindmap.repository.NoteRepository;
import com.mindmap.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * One-time seed script: inserts 30 realistic study notes across varied
 * subjects, topics, difficulties, and research areas into mindmap.sqlite.
 *
 * Run via Maven exec:
 *   ./mvnw compile exec:java -Dexec.mainClass=com.mindmap.SeedData
 */
public class SeedData {

    public static void main(String[] args) throws Exception {
        // Initialise DB schema if not yet done
        DatabaseInitializer.initialize();

        NoteRepository noteRepo = new NoteRepository();
        TagRepository tagRepo   = new TagRepository();

        List<NoteSpec> specs = buildSpecs();

        int saved = 0;
        for (NoteSpec s : specs) {
            Note note = new Note(s.title, s.content, s.subject, s.difficulty);
            note.setFavorite(s.favorite);
            note.setCreatedAt(s.createdAt);
            note.setUpdatedAt(s.createdAt);

            Note created = noteRepo.create(note);

            // Attach tags (resolve or create each)
            for (String tagName : s.tags) {
                Tag tag = tagRepo.findByName(tagName)
                        .orElseGet(() -> tagRepo.create(new Tag(tagName)));
                tagRepo.addTagToNote(created.getId(), tag.getId());
            }

            saved++;
            System.out.printf("[%2d] Saved: %-55s | subject=%-25s | diff=%-6s | tags=%s%n",
                    saved, s.title, s.subject, s.difficulty, Arrays.toString(s.tags));
        }

        System.out.println("\n✓ " + saved + " notes seeded into mindmap.sqlite");
    }

    // ─────────────────────────────────────────────────────────────
    //  Note specifications — 30 notes across 6 subjects, varied
    //  difficulty and research depth
    // ─────────────────────────────────────────────────────────────
    private static List<NoteSpec> buildSpecs() {
        LocalDateTime base = LocalDateTime.now().minusDays(30);

        return List.of(

            // ── Computer Science (6 notes) ──────────────────────────────
            new NoteSpec(
                "Big-O Notation and Complexity Classes",
                "Big-O notation expresses the upper bound of an algorithm's running time or space requirements as the input size grows. " +
                "Common complexity classes: O(1) constant, O(log n) logarithmic (binary search), O(n) linear (linear search), " +
                "O(n log n) linearithmic (merge sort, heap sort), O(n²) quadratic (bubble sort, insertion sort worst case), " +
                "O(2ⁿ) exponential (subset enumeration), O(n!) factorial (brute-force travelling salesman). " +
                "Always analyse both time and space complexity. Amortised analysis (e.g., ArrayList resizing) averages cost over many operations.",
                "Computer Science", "Easy",
                new String[]{"algorithms", "complexity", "computer-science"},
                base.minusDays(28), false
            ),

            new NoteSpec(
                "Binary Search Trees: Insertion, Deletion, Traversal",
                "A BST maintains the invariant: left subtree keys < root key < right subtree keys. " +
                "Insertion: traverse until empty slot matching invariant. " +
                "Search: O(h) where h is height; O(log n) balanced, O(n) degenerate. " +
                "Deletion: three cases — leaf (remove directly), one child (bypass), two children (replace with in-order successor). " +
                "Traversals: in-order (sorted output), pre-order (copy tree), post-order (delete tree), level-order (BFS). " +
                "Self-balancing variants: AVL trees (height difference ≤ 1), Red-Black trees (used in Java TreeMap).",
                "Computer Science", "Medium",
                new String[]{"data-structures", "BST", "trees"},
                base.minusDays(26), false
            ),

            new NoteSpec(
                "Dynamic Programming: Memoisation vs Tabulation",
                "Dynamic programming solves problems by breaking them into overlapping subproblems and caching results. " +
                "Two approaches: (1) Top-down memoisation — recursive solution with a cache (HashMap/array) to avoid recomputation. " +
                "(2) Bottom-up tabulation — fill a DP table iteratively from base cases up. " +
                "Classic examples: Fibonacci, 0/1 Knapsack, Longest Common Subsequence (LCS), Coin Change, Edit Distance. " +
                "Key property check: optimal substructure + overlapping subproblems. " +
                "Space optimisation: often reduce 2-D DP table to 1-D rolling array.",
                "Computer Science", "Hard",
                new String[]{"dynamic-programming", "algorithms", "optimisation"},
                base.minusDays(24), true
            ),

            new NoteSpec(
                "TCP/IP Model and the OSI Reference Model",
                "The OSI model has 7 layers: Physical, Data Link, Network, Transport, Session, Presentation, Application. " +
                "TCP/IP consolidates these into 4: Network Access (1+2), Internet (3), Transport (4), Application (5-7). " +
                "TCP (Transmission Control Protocol): reliable, connection-oriented, 3-way handshake (SYN, SYN-ACK, ACK), flow control, congestion control. " +
                "UDP: lightweight, connectionless, no guarantee — used for DNS, streaming, gaming. " +
                "IP addressing: IPv4 (32-bit, 4 octets), IPv6 (128-bit, hex). Subnetting: CIDR notation /24 = 255.255.255.0.",
                "Computer Science", "Medium",
                new String[]{"networking", "TCP/IP", "protocols"},
                base.minusDays(22), false
            ),

            new NoteSpec(
                "Operating Systems: Process vs Thread and Scheduling",
                "Process: independent execution unit with its own memory space, PCB, file descriptors. " +
                "Thread: lightweight unit within a process, shares heap/code/data but has own stack and registers. " +
                "Context switch: saving/restoring CPU state; expensive for processes, cheaper for threads. " +
                "Scheduling algorithms: FCFS (simple, convoy effect), SJF (optimal average wait, starvation risk), " +
                "Round Robin (preemptive, good responsiveness), Priority Scheduling, Multilevel Queue. " +
                "Deadlock: mutual exclusion, hold and wait, no preemption, circular wait — Banker's algorithm for avoidance.",
                "Computer Science", "Hard",
                new String[]{"operating-systems", "scheduling", "concurrency"},
                base.minusDays(20), false
            ),

            new NoteSpec(
                "REST API Design Principles",
                "REST (Representational State Transfer) defines six constraints: client-server, stateless, cacheable, " +
                "uniform interface, layered system, optional code-on-demand. " +
                "HTTP verbs: GET (read), POST (create), PUT/PATCH (update), DELETE. " +
                "Status codes: 2xx success, 3xx redirect, 4xx client error (404 Not Found, 400 Bad Request), 5xx server error. " +
                "Best practices: use nouns for resources (/users/{id}), version APIs (/v1/), return JSON, use HATEOAS for discoverability. " +
                "Authentication: API keys, OAuth 2.0, JWT Bearer tokens.",
                "Computer Science", "Easy",
                new String[]{"REST", "API", "web-development"},
                base.minusDays(18), true
            ),

            // ── Mathematics (5 notes) ───────────────────────────────────
            new NoteSpec(
                "Linear Algebra: Matrices and Transformations",
                "A matrix is a rectangular array of numbers. Matrix multiplication (A × B) is defined when cols(A) = rows(B); result is m×p for m×n · n×p. " +
                "Key operations: transpose (Aᵀ), inverse (A⁻¹ exists iff det(A) ≠ 0), determinant (area/volume scaling factor). " +
                "Linear transformations: rotation, scaling, shearing, reflection. Eigenvalues λ satisfy det(A − λI) = 0; eigenvectors span invariant subspaces. " +
                "Applications: PCA (dimensionality reduction), Google PageRank, computer graphics transforms, ML weight matrices.",
                "Mathematics", "Medium",
                new String[]{"linear-algebra", "matrices", "mathematics"},
                base.minusDays(27), false
            ),

            new NoteSpec(
                "Calculus: Derivatives and the Chain Rule",
                "The derivative f'(x) = lim[h→0] (f(x+h) − f(x)) / h measures the instantaneous rate of change. " +
                "Key rules: power rule d/dx[xⁿ] = nxⁿ⁻¹, product rule d/dx[uv] = u'v + uv', quotient rule, chain rule d/dx[f(g(x))] = f'(g(x))·g'(x). " +
                "Chain rule applications: backpropagation in neural networks, implicit differentiation. " +
                "Higher-order derivatives: f'' gives concavity, f'' > 0 → concave up (local min), f'' < 0 → concave down (local max). " +
                "L'Hôpital's rule: for 0/0 or ∞/∞ indeterminate forms, lim f/g = lim f'/g'.",
                "Mathematics", "Medium",
                new String[]{"calculus", "derivatives", "mathematics"},
                base.minusDays(25), false
            ),

            new NoteSpec(
                "Probability Theory: Bayes' Theorem",
                "Bayes' theorem: P(A|B) = P(B|A) × P(A) / P(B). " +
                "P(A) is the prior (belief before evidence), P(B|A) is the likelihood, P(A|B) is the posterior. " +
                "Law of Total Probability: P(B) = Σ P(B|Aᵢ)P(Aᵢ). " +
                "Applications: spam filters (Naive Bayes classifier), medical diagnosis, Bayesian inference in ML. " +
                "Random variables: discrete (PMF, E[X] = Σ x·P(X=x)) vs continuous (PDF, E[X] = ∫ x·f(x)dx). " +
                "Common distributions: Bernoulli, Binomial, Poisson, Normal (Gaussian), Exponential.",
                "Mathematics", "Hard",
                new String[]{"probability", "statistics", "Bayes"},
                base.minusDays(23), true
            ),

            new NoteSpec(
                "Number Theory: Prime Numbers and Modular Arithmetic",
                "A prime p > 1 is divisible only by 1 and p. Fundamental Theorem of Arithmetic: every integer ≥ 2 has a unique prime factorisation. " +
                "Sieve of Eratosthenes: finds all primes ≤ n in O(n log log n). " +
                "Modular arithmetic: a ≡ b (mod m) means m | (a−b). Properties: (a+b) mod m, (a·b) mod m. " +
                "Fermat's Little Theorem: aᵖ⁻¹ ≡ 1 (mod p) for prime p, gcd(a,p)=1. " +
                "RSA encryption relies on the difficulty of factoring large composites: n = p·q, public key e, private key d where e·d ≡ 1 (mod φ(n)).",
                "Mathematics", "Hard",
                new String[]{"number-theory", "cryptography", "mathematics"},
                base.minusDays(21), false
            ),

            new NoteSpec(
                "Graph Theory: Shortest Path Algorithms",
                "A graph G = (V, E) consists of vertices and edges (directed or undirected, weighted or unweighted). " +
                "Dijkstra's algorithm: greedily picks minimum-distance unvisited node; O((V+E) log V) with min-heap; requires non-negative weights. " +
                "Bellman-Ford: handles negative weights; detects negative cycles; O(V·E). " +
                "Floyd-Warshall: all-pairs shortest paths; O(V³); DP on intermediate vertices. " +
                "BFS: unweighted shortest path O(V+E). " +
                "Applications: GPS navigation, network routing (OSPF uses Dijkstra), social network degrees of separation.",
                "Mathematics", "Hard",
                new String[]{"graph-theory", "algorithms", "shortest-path"},
                base.minusDays(19), false
            ),

            // ── Physics (5 notes) ───────────────────────────────────────
            new NoteSpec(
                "Newton's Laws of Motion",
                "First Law (Inertia): An object at rest stays at rest and an object in motion stays in motion unless acted upon by a net external force. " +
                "Second Law: F = ma — net force equals mass times acceleration. Vector equation; direction matters. " +
                "Third Law: For every action there is an equal and opposite reaction. Forces come in pairs but act on different objects. " +
                "Applications: free body diagrams, normal force on inclined planes, friction (f = μN), tension in ropes, circular motion (centripetal a = v²/r). " +
                "Momentum: p = mv; Impulse-Momentum theorem: J = Δp = F·Δt.",
                "Physics", "Easy",
                new String[]{"mechanics", "Newton", "physics"},
                base.minusDays(27), false
            ),

            new NoteSpec(
                "Thermodynamics: Laws and Entropy",
                "Zeroth Law: if A is in thermal equilibrium with B and B with C, then A is in equilibrium with C — defines temperature. " +
                "First Law: ΔU = Q − W — energy is conserved; internal energy change equals heat added minus work done by system. " +
                "Second Law: entropy of an isolated system never decreases; heat flows spontaneously from hot to cold. " +
                "Third Law: entropy approaches a minimum as temperature approaches absolute zero. " +
                "Entropy S = k_B ln(Ω); Carnot efficiency η = 1 − T_cold/T_hot. " +
                "Applications: heat engines, refrigerators, information theory (Shannon entropy).",
                "Physics", "Medium",
                new String[]{"thermodynamics", "entropy", "physics"},
                base.minusDays(25), false
            ),

            new NoteSpec(
                "Quantum Mechanics: Wave-Particle Duality and Uncertainty",
                "De Broglie hypothesis: all matter has an associated wavelength λ = h/p where h is Planck's constant. " +
                "Wave function ψ(x,t): complex-valued; |ψ|² gives probability density. Schrödinger equation: iħ ∂ψ/∂t = Ĥψ. " +
                "Heisenberg Uncertainty Principle: Δx · Δp ≥ ħ/2 — position and momentum cannot both be known precisely. " +
                "Superposition: a quantum system exists in multiple states until measured (Copenhagen interpretation). " +
                "Double-slit experiment: single particles show interference patterns, demonstrating wave behaviour. " +
                "Applications: lasers, MRI, transistors, quantum computing (qubits in superposition).",
                "Physics", "Hard",
                new String[]{"quantum-mechanics", "physics", "wave-particle"},
                base.minusDays(23), true
            ),

            new NoteSpec(
                "Electromagnetism: Maxwell's Equations",
                "Maxwell's four equations unify electricity, magnetism and optics: " +
                "(1) Gauss's Law: ∇·E = ρ/ε₀ — electric flux through closed surface = enclosed charge / ε₀. " +
                "(2) Gauss's Law for Magnetism: ∇·B = 0 — no magnetic monopoles. " +
                "(3) Faraday's Law: ∇×E = −∂B/∂t — changing magnetic field induces electric field. " +
                "(4) Ampère-Maxwell Law: ∇×B = μ₀J + μ₀ε₀∂E/∂t — current and changing electric field produce magnetic field. " +
                "These predict electromagnetic waves travelling at c = 1/√(μ₀ε₀) ≈ 3×10⁸ m/s.",
                "Physics", "Hard",
                new String[]{"electromagnetism", "Maxwell", "physics"},
                base.minusDays(21), false
            ),

            new NoteSpec(
                "Special Relativity: Time Dilation and Length Contraction",
                "Einstein's two postulates: (1) laws of physics are the same in all inertial frames; (2) speed of light c is constant in all frames. " +
                "Time dilation: Δt = γΔt₀ where γ = 1/√(1−v²/c²) — moving clocks run slow. " +
                "Length contraction: L = L₀/γ — moving objects are shorter in the direction of motion. " +
                "Simultaneity is relative — events simultaneous in one frame may not be in another. " +
                "Mass-energy equivalence: E = mc². Rest energy + kinetic energy: E² = (pc)² + (mc²)². " +
                "GPS satellites must account for both special and general relativistic effects.",
                "Physics", "Hard",
                new String[]{"relativity", "Einstein", "physics"},
                base.minusDays(19), false
            ),

            // ── Biology (5 notes) ───────────────────────────────────────
            new NoteSpec(
                "Cell Biology: Structure and Function of Organelles",
                "Eukaryotic cells contain membrane-bound organelles: " +
                "Nucleus — stores DNA; double membrane with nuclear pores; contains nucleolus (rRNA synthesis). " +
                "Mitochondria — ATP production via cellular respiration; cristae increase surface area; contain own DNA (endosymbiotic theory). " +
                "Endoplasmic Reticulum — rough ER (ribosomes, protein synthesis), smooth ER (lipid synthesis, detoxification). " +
                "Golgi apparatus — sorting, modifying, packaging proteins for secretion. " +
                "Lysosomes — enzymatic digestion; pH ~5. Chloroplasts (plants) — photosynthesis, contain chlorophyll.",
                "Biology", "Easy",
                new String[]{"cell-biology", "organelles", "biology"},
                base.minusDays(26), false
            ),

            new NoteSpec(
                "DNA Replication and the Central Dogma",
                "Central Dogma: DNA → (Transcription) → mRNA → (Translation) → Protein. " +
                "DNA replication is semi-conservative: each daughter strand contains one original and one new strand. " +
                "Key enzymes: Helicase (unwinds double helix), Primase (lays RNA primer), DNA Polymerase III (5'→3' synthesis), " +
                "DNA Polymerase I (replaces primer), DNA Ligase (seals nicks). " +
                "Leading strand: continuous synthesis; Lagging strand: Okazaki fragments joined by ligase. " +
                "Transcription: RNA Pol II synthesises pre-mRNA; splicing removes introns, joins exons. " +
                "Translation: ribosome reads mRNA codons; tRNA brings amino acids; start codon AUG (Met), stop codons UAA/UAG/UGA.",
                "Biology", "Medium",
                new String[]{"DNA", "genetics", "molecular-biology"},
                base.minusDays(24), true
            ),

            new NoteSpec(
                "Evolution: Natural Selection and Speciation",
                "Darwin's theory of natural selection: variation exists in populations; traits are heritable; " +
                "organisms with favourable traits survive and reproduce more (differential fitness). " +
                "Types of selection: directional (shifts phenotype mean), stabilising (reduces variation), disruptive (favours extremes). " +
                "Genetic drift: random allele frequency changes; more pronounced in small populations (founder effect, bottleneck). " +
                "Speciation: allopatric (geographic isolation → reproductive isolation), sympatric (no geographic barrier, e.g., polyploidy in plants). " +
                "Hardy-Weinberg equilibrium: p² + 2pq + q² = 1; maintained when no mutation, migration, drift, selection, random mating.",
                "Biology", "Medium",
                new String[]{"evolution", "genetics", "natural-selection"},
                base.minusDays(22), false
            ),

            new NoteSpec(
                "Immunology: Innate vs Adaptive Immunity",
                "Innate immunity: fast, non-specific first line of defence. Barriers (skin, mucus), phagocytes (neutrophils, macrophages), " +
                "natural killer cells, complement system, inflammation, fever. Pattern recognition via Toll-like receptors (TLRs). " +
                "Adaptive immunity: slow (days), antigen-specific, memory. " +
                "Humoral: B cells → plasma cells → antibodies (IgG, IgM, IgA, IgE, IgD); opsonisation, neutralisation, complement activation. " +
                "Cell-mediated: T cells — helper T (CD4⁺, activate B cells and macrophages), cytotoxic T (CD8⁺, kill infected cells). " +
                "MHC class I (all nucleated cells), class II (APCs). Clonal selection and immunological memory underlie vaccination.",
                "Biology", "Hard",
                new String[]{"immunology", "biology", "immunity"},
                base.minusDays(20), false
            ),

            new NoteSpec(
                "Genetics: Mendelian Inheritance and Linkage",
                "Mendel's laws: Law of Segregation (each individual has two alleles, only one passed to offspring), " +
                "Law of Independent Assortment (genes on different chromosomes assort independently — not true for linked genes). " +
                "Monohybrid cross Aa × Aa → 1 AA : 2 Aa : 1 aa (3:1 phenotype ratio). " +
                "Dihybrid cross AaBb × AaBb → 9:3:3:1. " +
                "Exceptions: incomplete dominance (Aa = intermediate), codominance (ABO blood group), epistasis, pleiotropy, polygenic traits. " +
                "Linked genes: located on same chromosome; recombination frequency measures map distance (1 cM = 1% recombination). " +
                "Sex-linked traits (X-linked): affected males = Xᵃ Y; carrier females = X Xᵃ.",
                "Biology", "Medium",
                new String[]{"genetics", "Mendel", "inheritance"},
                base.minusDays(18), false
            ),

            // ── Artificial Intelligence / Machine Learning (5 notes) ────
            new NoteSpec(
                "Neural Networks: Forward and Backpropagation",
                "A feedforward neural network has layers of neurons: input, hidden, output. " +
                "Each neuron computes: z = Wx + b; activation a = σ(z) (e.g., ReLU, sigmoid, tanh, softmax). " +
                "Forward pass: compute activations layer by layer to get prediction. " +
                "Loss function: MSE for regression, cross-entropy for classification. " +
                "Backpropagation: compute ∂L/∂W for each layer using chain rule. " +
                "Gradient descent update: W ← W − η·∂L/∂W. " +
                "Variants: SGD, Mini-batch GD, Adam (adaptive learning rates). " +
                "Vanishing gradient: sigmoid/tanh saturate; ReLU and batch normalisation mitigate this.",
                "Artificial Intelligence", "Medium",
                new String[]{"neural-networks", "deep-learning", "AI"},
                base.minusDays(27), true
            ),

            new NoteSpec(
                "Support Vector Machines and the Kernel Trick",
                "SVM finds the hyperplane that maximises the margin between two classes. " +
                "Support vectors: data points closest to the hyperplane — only these define the decision boundary. " +
                "Hard-margin SVM: perfectly separable data. Soft-margin SVM: allows misclassification with slack variables ξᵢ; " +
                "regularisation parameter C controls bias-variance tradeoff. " +
                "Kernel trick: implicitly maps data to higher-dimensional space without computing coordinates. " +
                "Common kernels: linear, polynomial K(x,z) = (xᵀz + c)^d, RBF/Gaussian K(x,z) = exp(−γ‖x−z‖²). " +
                "Mercer's condition ensures kernel corresponds to a valid inner product. " +
                "SVMs are effective in high dimensions; sensitive to feature scaling.",
                "Artificial Intelligence", "Hard",
                new String[]{"SVM", "machine-learning", "classification"},
                base.minusDays(25), false
            ),

            new NoteSpec(
                "Transformer Architecture and Self-Attention",
                "Transformers replaced RNNs for sequence modelling by using self-attention — computing relationships between all positions in parallel. " +
                "Attention: Attention(Q,K,V) = softmax(QKᵀ / √d_k) · V. Queries, Keys, Values are linear projections of input embeddings. " +
                "Multi-head attention: h parallel attention heads capture different aspects. " +
                "Positional encoding: sinusoidal or learned vectors added to embeddings (no inherent order in attention). " +
                "Encoder-decoder: BERT (encoder-only, bidirectional), GPT (decoder-only, causal), T5 (encoder-decoder). " +
                "Pre-training on large corpora + fine-tuning on downstream tasks (transfer learning). " +
                "Scale laws: performance improves predictably with model size, data, and compute.",
                "Artificial Intelligence", "Hard",
                new String[]{"transformers", "NLP", "deep-learning"},
                base.minusDays(23), true
            ),

            new NoteSpec(
                "Reinforcement Learning: MDP and Q-Learning",
                "RL framework: agent interacts with environment, receives reward r_t, transitions to new state s_{t+1}. " +
                "Markov Decision Process (MDP): tuple (S, A, P, R, γ). Discount factor γ ∈ [0,1] weights future rewards. " +
                "Value function V^π(s) = E[Σ γᵗ rₜ | s₀=s, π]. Bellman equation: V*(s) = max_a [R(s,a) + γ Σ P(s'|s,a) V*(s')]. " +
                "Q-learning: model-free, off-policy. Update: Q(s,a) ← Q(s,a) + α[r + γ max_a' Q(s',a') − Q(s,a)]. " +
                "Deep Q-Network (DQN): neural net approximates Q-function; experience replay, target network for stability. " +
                "Policy gradient methods (REINFORCE, PPO, A3C): directly optimise policy π_θ.",
                "Artificial Intelligence", "Hard",
                new String[]{"reinforcement-learning", "Q-learning", "AI"},
                base.minusDays(21), false
            ),

            new NoteSpec(
                "Clustering Algorithms: k-Means and DBSCAN",
                "k-Means: partition n points into k clusters. Algorithm: (1) initialise k centroids (k-means++ improves this), " +
                "(2) assign each point to nearest centroid, (3) recompute centroids as cluster means, (4) repeat until convergence. " +
                "Objective: minimise within-cluster sum of squared distances (inertia). " +
                "Elbow method / silhouette score to choose k. Sensitive to outliers and non-spherical clusters. " +
                "DBSCAN: density-based; defines core points (≥ MinPts neighbours within ε radius), border points, noise. " +
                "Advantages: discovers arbitrary shapes, automatically determines cluster count, robust to outliers. " +
                "Disadvantage: struggles with varying density clusters.",
                "Artificial Intelligence", "Medium",
                new String[]{"clustering", "unsupervised-learning", "machine-learning"},
                base.minusDays(19), false
            ),

            // ── Research / Interdisciplinary (4 notes) ──────────────────
            new NoteSpec(
                "Scientific Method and Research Design",
                "The scientific method: observation → question → hypothesis → experiment → data collection → analysis → conclusion → peer review. " +
                "Experimental design: independent variable (manipulated), dependent variable (measured), control group, confounding variables. " +
                "Types of studies: randomised controlled trial (gold standard), cohort (prospective/retrospective), case-control, cross-sectional. " +
                "Statistical concepts: null hypothesis H₀, p-value (probability of observed result under H₀), significance level α (typically 0.05), " +
                "Type I error (false positive), Type II error (false negative), power (1 − β). " +
                "Publication bias: positive results more likely published; meta-analysis aggregates multiple studies.",
                "Research Methods", "Medium",
                new String[]{"research-methods", "statistics", "scientific-method"},
                base.minusDays(17), false
            ),

            new NoteSpec(
                "Literature Review: Techniques and Citation Management",
                "A literature review synthesises existing research to identify gaps and contextualise your study. " +
                "Systematic review: reproducible search strategy with inclusion/exclusion criteria (PRISMA guidelines). " +
                "Databases: Google Scholar, PubMed, IEEE Xplore, Scopus, ACM Digital Library, arXiv. " +
                "Citation styles: APA (Author, Year), MLA (Author Page), IEEE [numbered], Chicago. " +
                "Tools: Zotero (free, open source), Mendeley, EndNote for citation management. " +
                "Thematic synthesis: group papers by theme. Critical appraisal: evaluate methodology, sample size, bias, validity. " +
                "Forward and backward citation chaining to discover related work.",
                "Research Methods", "Easy",
                new String[]{"research-methods", "literature-review", "academic-writing"},
                base.minusDays(15), false
            ),

            new NoteSpec(
                "Data Analysis with Python: Pandas and NumPy",
                "NumPy: N-dimensional array (ndarray); vectorised operations avoid Python loops (C-speed). " +
                "Broadcasting: operations on arrays of different shapes; rules: dimensions compatible if equal or one is 1. " +
                "Pandas: DataFrame (2D labelled table), Series (1D). " +
                "Key operations: df.groupby(), df.merge(), df.pivot_table(), df.apply(), df.dropna(), df.fillna(). " +
                "Data pipeline: load (read_csv/read_excel/read_sql) → explore (head, describe, info) → clean → transform → visualise (matplotlib/seaborn). " +
                "Performance: use vectorised operations; avoid iterrows(); prefer .loc/.iloc for indexing. " +
                "Jupyter notebooks provide interactive, reproducible analysis with markdown documentation.",
                "Research Methods", "Medium",
                new String[]{"Python", "data-analysis", "pandas"},
                base.minusDays(13), true
            ),

            new NoteSpec(
                "Ethical Considerations in AI Research",
                "Fairness: ML models can perpetuate or amplify societal biases present in training data. " +
                "Metrics: demographic parity, equal opportunity, calibration — these can be mutually exclusive (impossibility theorems). " +
                "Explainability (XAI): LIME (local linear approximations), SHAP (Shapley values from game theory). " +
                "Privacy: differential privacy adds calibrated noise so individual records are protected while statistics are preserved. " +
                "Federated learning: train on local data, aggregate gradients — data never leaves devices. " +
                "Regulatory frameworks: EU AI Act (risk-based tiers), GDPR (right to explanation), IEEE Ethically Aligned Design. " +
                "Model cards and datasheets: transparency documents for models and datasets.",
                "Research Methods", "Hard",
                new String[]{"AI-ethics", "fairness", "research-methods"},
                base.minusDays(11), true
            )
        );
    }

    // ─── Helper record ───────────────────────────────────────────
    record NoteSpec(String title, String content, String subject, String difficulty,
                    String[] tags, LocalDateTime createdAt, boolean favorite) {}
}
