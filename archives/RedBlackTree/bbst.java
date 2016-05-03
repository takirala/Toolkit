/**
 * @author Tarun Gupta Akirala
 * @version 1.0
 * <p> Created by takirala on 3/11/2016. </p>
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class bbst {

    /**
     * <pre>
     *     The input file format should be like:
     *
     *             number_of_nodes
     *             id1 count1
     *             id2 count2
     *             id3 count3
     *             .
     *             .
     *             .
     *             and so on.
     *             Main method will do :
     *             1. Build the tree
     *             2. Execute the commands read from standard input - {increase, reduce, count, inrage, next, previous, quit}
     *             3. When quit command is entered, the program exits with status code 0.
     * </pre>
     *
     * @param args - Only the first argument is considered - It should be the input file in above format.
     */
    public static void main(String[] args) {

        try {
            if (args.length == 0) {
                System.err.println("No arguments found. Run with only one argument as the input file name.");
                return;
            }

            File f = new File(args[0]);
            if (!f.exists()) {
                System.err.println("No file found : " + args[0]);
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(f));
            String nodesStr = br.readLine();

            int capacity = Integer.parseInt(nodesStr);
            if (capacity <= 0) {
                System.err.println("Invalid input data. First line reads : " + capacity);
                return;
            }

            EventCounter eventCounter = new EventCounter();

            //ArrayList<Integer> keys = new ArrayList<Integer>(capacity);
            //ArrayList<Integer> vals = new ArrayList<Integer>(capacity);
            int[] keys = new int[capacity];
            int[] vals = new int[capacity];

            // Build the tree.
            String line = "";
            int counter = 0;
            while ((line = br.readLine()) != null) {
                String[] elems = line.split(" ");
                keys[counter] = Integer.parseInt(elems[0]);
                vals[counter] = Integer.parseInt(elems[1]);
                counter++;
            }

            // Linear time building.
            eventCounter.buildTree(keys, vals);

            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                String command = sc.next();
                //System.out.println("Command " + command);
                switch (command) {
                    case "increase":
                        eventCounter.increase(sc.nextInt(), sc.nextInt());
                        break;
                    case "reduce":
                        eventCounter.reduce(sc.nextInt(), sc.nextInt());
                        break;
                    case "count":
                        eventCounter.count(sc.nextInt());
                        break;
                    case "inrange":
                        eventCounter.inRange(sc.nextInt(), sc.nextInt());
                        break;
                    case "next":
                        eventCounter.next(sc.nextInt());
                        break;
                    case "previous":
                        eventCounter.previous(sc.nextInt());
                        break;
                    case "quit":
                    default:
                        System.exit(0);
                }
                sc.nextLine();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}


class EventCounter {

    /**
     * By convention, RED is 0 and BLACK is -1.
     */
    static final byte RED = 0;
    static final byte BLACK = -1;

    /**
     * TreeNode structure has left, right pointers, color, data and nodes attributes.
     */
    class TreeNode {
        int ID;
        int count;
        byte color;

        /**
         * Pointers to left, right and parent nodes.
         */
        TreeNode left, right, parent;

        /**
         * @param ID    the ID value of the event. This is used as sorting key for the Red Black Tree
         * @param count the count value associated with the ID
         * @param color the color value of the node
         *              <p/>
         *              The left, right & parent nodes are not initialized in the constructor.
         */
        TreeNode(int ID, int count, byte color) {
            this.ID = ID;
            this.count = count;
            this.color = color;
        }
    }

    // Root node is class variable used for several helper functions.
    private TreeNode root;

    /**
     * Inserts the node into the tree.
     *
     * @param ID    The ID value to be inserted.
     * @param count The event count for the corresponding ID.
     */
    void insert(Integer ID, Integer count) {
        upsert(ID, count, false);
    }

    /**
     * If the node is not already present, this will insert a new node.
     * If the node is present, this operation will increase the value of the event count associated with that node.
     *
     * @param ID    The ID value to be inserted.
     * @param count The event count for the corresponding ID.
     */
    void increase(Integer ID, Integer count) {
        upsert(ID, count, true);
    }

    /**
     * If the node is not already present, this operation does not do anything.
     * If the node is present, this operation will decrease the value of the event count associated with that node.
     * If the updated value goes below 1, the node is delted and the new tree is fixed if needed.
     *
     * @param ID    The ID value to be inserted.
     * @param count The event count for the corresponding ID.
     */
    void reduce(Integer ID, Integer count) {
        TreeNode existing = getNode(ID);
        if (existing == null) {
            System.out.println(0);
        } else if (existing.count < count) {
            System.out.println(0);
            delete(ID);
        } else {
            upsert(ID, -count, true);
        }
    }

    /**
     * Prints the event count associated with the passed ID.
     *
     * @param ID The event ID whose count has to be found. This performs a binary search.
     */
    void count(Integer ID) {
        TreeNode current = getNode(ID);
        System.out.println(current == null ? 0 : current.count);
    }

    /**
     * Print the total count for IDs between ID1 and ID2 inclusively. Note, ID1 ≤ ID2
     *
     * @param ID1 The smaller event ID
     * @param ID2 The bigger event ID
     */
    void inRange(Integer ID1, Integer ID2) {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        populateList(root, nodes, ID1, ID2);
        int totalCount = 0;
        for (TreeNode n : nodes) {
            totalCount += n.count;
        }
        System.out.println(totalCount);
    }

    /**
     * Print the ID and the count of the event with the lowest ID that is greater that theID. Print 0 0, if there is no next ID.
     *
     * @param ID This ID may or may not be present in the tree. The next biggest ID is printed.
     */
    void next(Integer ID) {
        TreeNode successor = findSuccessor(root, ID);
        String result = successor == null ? "0 0" : successor.ID + " " + successor.count;
        System.out.println(result);
    }

    /**
     * Print the ID and the count of the event with the highest ID that is less that theID. Print 0 0 if there is no previous ID.
     *
     * @param ID This ID may or may not be present in the tree. The previously biggest ID is printed.
     */
    void previous(Integer ID) {
        TreeNode predecessor = findPredecessor(root, ID);
        String result = predecessor == null ? "0 0" : predecessor.ID + " " + predecessor.count;
        System.out.println(result);
    }


    /**
     * This builds the tree in linear time
     * @param keys The array of IDs
     * @param vals The array of counts
     */
    void buildTree(int[] keys, int[] vals) {
        root = null;
        long t1 = System.currentTimeMillis();
        root = buildTree(0, keys.length - 1, keys, vals);

        root.parent = null;
        int leftHeight = getLeftHeight(root);
        int rightHeight = getRightHeight(root);
        if (leftHeight != rightHeight) {
            markRed(root, 0, Math.max(leftHeight, rightHeight));
        }
    }

    /**
     * Using the keys and vals arrays for ID, count values, the node is build and then its left and right children and built recursively.
     *
     */
    private TreeNode buildTree(int low, int high, int[] keys, int[] vals) {
        if (low > high) return null;
        int mid = (low + high) / 2;

        TreeNode node = new TreeNode(keys[mid], vals[mid], BLACK);
        node.left = buildTree(low, mid - 1, keys, vals);
        node.right = buildTree(mid + 1, high, keys, vals);
        if (node.left != null) {
            node.left.parent = node;
        }

        if (node.right != null) {
            node.right.parent = node;
        }
        return node;
    }

    private int getLeftHeight(TreeNode node) {
        if (node == null) return 0;

        int height = 0;
        while (node.left != null) {
            height++;
            node = node.left;
        }
        return height;
    }

    private int getRightHeight(TreeNode n) {
        if (n == null) return 0;

        int height = 0;
        while (n.right != null) {
            height++;
            n = n.right;
        }
        return height;
    }

    private void markRed(TreeNode node, int curLevel, int lastLevel) {
        if (node == null) return;
        if (curLevel == lastLevel) {
            node.color = RED;
        } else {
            markRed(node.left, curLevel + 1, lastLevel);
            markRed(node.right, curLevel + 1, lastLevel);
        }
    }
    // ########################### Util functions.###########################

    /**
     * Checks if a node color is red or not. Null nodes are black by assumption.
     */
    private boolean isRed(TreeNode node) {
        return (node != null && node.color == RED) ? true : false;
    }

    // ########################### RBT Insert ###########################

    /**
     * Search for an ID in O(log(n)) time - Binary search.
     * Find count value corresponding to a specific ID. If the ID is not found, null is returned.
     */
    private TreeNode getNode(Integer ID) {
        TreeNode node = root;
        while (node != null) {
            if (ID < node.ID) node = node.left;
            else if (ID > node.ID) node = node.right;
            else return node;
        }
        return null;
    }

    /**
     * This will update / insert accordingly
     */
    private void upsert(Integer ID, Integer count, boolean doPrint) {
        if (ID == null) throw new NullPointerException("Invalid ID.");
        if (count == null) {
            delete(ID);
            return;
        }
        // Special case if root is null.
        if (root == null) {
            root = new TreeNode(ID, count, BLACK);
            if (doPrint) System.out.println(count);
            root.parent = null;
        } else {
            root = upsert(root, ID, count, doPrint);
            root.color = BLACK;
            root.parent = null;
        }
    }

    /**
     * This will update / insert accordingly. After the node is inserted / updated fix up is called to fix any violations
     *
     * @param node    The search node pointer. Used in recursive calls.
     * @param ID      The new ID whihc is to be updated or inserted.
     * @param count   The event count value corresponding to the ID.
     * @param doPrint A boolean value that controls the printing of count of new node.
     * @return return the parent node of the current recursive call. Return root in the last recursive call
     */
    private TreeNode upsert(TreeNode node, Integer ID, Integer count, boolean doPrint) {
        if (node == null) {
            if (doPrint) System.out.println(count);
            return new TreeNode(ID, count, RED);
        }

        // Find ID recursively. Top to bottom
        if (ID < node.ID) {
            TreeNode temp = upsert(node.left, ID, count, doPrint);
            temp.parent = node;
            node.left = temp;
            node.left.parent = node;
        } else if (ID > node.ID) {
            TreeNode temp = upsert(node.right, ID, count, doPrint);
            temp.parent = node;
            node.right = temp;
            node.right.parent = node;
        } else {
            int newCount = node.count + count;
            if (newCount <= 0) {
                delete(ID);
                newCount = 0;
            } else
                node.count = newCount;
            if (doPrint) System.out.println(newCount);
        }

        // fix-up any violations.
        // This will be called on the way up. Up to root. Bottom to Top.
        node = fixViolations(node);
        return node;
    }

    // ########################### Helpers for RBT Insert.###########################

    /**
     * Check the invariants of RB Tree. This also makes sure that the tree is always balanced.
     *
     * @param node calls all the three fixes on the current node if applicable.
     * @return the fixed up node.
     */
    private TreeNode fixViolations(TreeNode node) {

        if (isRed(node.right)) node = leftRotate(node);
        if (isRed(node.left) && isRed(node.left.left)) node = rightRotate(node);
        if (isRed(node.left) && isRed(node.right)) swapColors(node);
        return node;
    }

    /**
     * Performs right rotation on the current node
     */
    private TreeNode rightRotate(TreeNode node) {
        TreeNode z = node.left;

        // Move the child node
        node.left = z.right;
        if (node.left != null) node.left.parent = node;

        // Mpve the node itself
        z.right = node;
        z.parent = node.parent;
        z.right.parent = z;

        // Fix the colors
        z.color = z.right.color;
        z.right.color = RED;
        return z;
    }

    /**
     * Performs left rotation on the current node
     */
    private TreeNode leftRotate(TreeNode node) {
        TreeNode z = node.right;

        // Move child node
        node.right = z.left;
        if (node.right != null) node.right.parent = node;

        // Swap the node.
        z.left = node;
        z.parent = node.parent;
        z.left.parent = z;

        // Fix the colors.
        z.color = z.left.color;
        z.left.color = RED;
        return z;
    }


    /**
     * alters the colors of children and parent node.
     */
    private void swapColors(TreeNode node) {
        node.left.color = (byte) ~node.left.color;
        node.right.color = (byte) ~node.right.color;
        node.color = (byte) ~node.color;
    }

    // ########################### RBT Delete ###########################

    /**
     * delete the key-value pair with the minimum key rooted at node
     *
     * @param node Node at which the minimum element must be deleted.
     */
    private TreeNode delMinimum(TreeNode node) {

        if (node.left == null)
            return null;

        if (!isRed(node.left) && !isRed(node.left.left))
            node = createRedLeft(node);

        node.left = delMinimum(node.left);
        return fixViolations(node);
    }

    /**
     * Deletes the node with the corresponding ID.
     *
     * @param ID ID to be searched and deleted.
     */
    private void delete(Integer ID) {
        if (getNode(ID) == null) return;

        // if both children of root are black, set root to red
        if (!isRed(root.left) && !isRed(root.right)) root.color = RED;

        root = delete(root, ID);
        if (root != null) {
            root.color = BLACK;
            root.parent = null;
        }
    }

    /**
     * Deletes the node with the corresponding ID.
     *
     * @param ID ID to be searched and deleted.
     */
    private TreeNode delete(TreeNode node, Integer ID) {
        if (ID.compareTo(node.ID) < 0) {
            if (!isRed(node.left) && !isRed(node.left.left))
                node = createRedLeft(node);
            node.left = delete(node.left, ID);
            if (node.left != null) node.left.parent = node;
        } else {

            // Do a right rotate.
            if (isRed(node.left)) {
                node = rightRotate(node);
            }

            // If the current ID is the ID to be deleted, return null to set the node to null.
            if (ID.equals(node.ID) && (node.right == null)) {
                return null; //If right child is null, inorder successor is null.
            }

            // If right node and left child of right node are black, create a red colored node to delete.
            if (!isRed(node.right) && !isRed(node.right.left)) {
                node = createRedRight(node);
            }

            if (ID.equals(node.ID)) {
                // COpy the ID and count from inorder successor on right child.
                TreeNode z = leftMost(node.right);
                node.ID = z.ID;
                node.count = z.count;
                // Delete the successor on right child.
                node.right = delMinimum(node.right);
                if (node.right != null) node.right.parent = node;
            } else {
                node.right = delete(node.right, ID);
                if (node.right != null) node.right.parent = node;
            }
        }
        return fixViolations(node);
    }

    private TreeNode leftMost(TreeNode node) {
        if (node.left == null) return node;
        else return leftMost(node.left);
    }

    /**
     * node - Red
     * node.left and node.left.left are Black
     * Create a red node in node.left or one of its children.
     */
    private TreeNode createRedLeft(TreeNode node) {
        swapColors(node);
        if (isRed(node.right.left)) {
            node.right = rightRotate(node.right);
            node.right.parent = node;
            node = leftRotate(node);
            swapColors(node);
        }
        return node;
    }

    /**
     * node - Red
     * node.right and node.right.left - Black
     * Create a red node in node.right or one of its children.
     * Created red node will compensate in deletion deficiency.
     */
    private TreeNode createRedRight(TreeNode node) {
        swapColors(node);
        if (isRed(node.left.left)) {
            TreeNode parent = node.parent;
            node = rightRotate(node);
            node.parent = parent;
            swapColors(node);
        }
        return node;
    }

    /**
     * Add all nodes between ID1 and ID2 (inclusive) to list
     */
    private void populateList(TreeNode node, ArrayList<TreeNode> nodes, Integer ID1, Integer ID2) {
        if (node == null || (ID1 > ID2)) return;
        if (ID1 <= node.ID && node.ID <= ID2) {
            nodes.add(node);
            populateList(node.left, nodes, ID1, ID2);
            populateList(node.right, nodes, ID1, ID2);
        } else if (node.ID < ID1) {
            populateList(node.right, nodes, ID1, ID2);
        } else if (ID2 < node.ID) {
            populateList(node.left, nodes, ID1, ID2);
        } else {
            // Does not happen
        }
    }

    /**
     * @param node    The node whose successor has to be found.
     * @param eventId The node that we are search for should have a value greater than this eventId
     * @return The successor node
     */
    private TreeNode findSuccessor(TreeNode node, Integer eventId) {
        if (node == null) return null;

        if (node.ID <= eventId) return findSuccessor(node.right, eventId);
        TreeNode t = findSuccessor(node.left, eventId);
        if (t != null) return t;
        else return node;
    }

    /**
     * @param node    The node whose predecessor has to be found.
     * @param eventId The node that we are search for should have a value less than this eventId
     * @return The predecessor node
     */
    private TreeNode findPredecessor(TreeNode node, Integer eventId) {
        if (node == null) return null;

        if (node.ID >= eventId) return findPredecessor(node.left, eventId);
        TreeNode t = findPredecessor(node.right, eventId);
        if (t != null) return t;
        else return node;
    }

    void printTree() {
        Deque<TreeNode> bfs = new ArrayDeque<TreeNode>();
        bfs.add(root);
        int c = 1;
        TreeNode dummy = new TreeNode((Integer) null, (Integer) null, BLACK);
        while (bfs.size() > 0) {
            boolean once = false;
            for (int i = 0; i < c; i++) {
                TreeNode temp = bfs.pollFirst();
                if (temp.ID == 0) {
                    bfs.addLast(dummy);
                    bfs.addLast(dummy);
                } else {
                    //Object b = temp.parent == null ? "" : " L: " + (temp.parent.left == null ? "" : temp.parent.left.ID) + " R: " + (temp.parent.right == null ? "" : temp.parent.right.ID) + " P: " + (temp.parent == null ? "" : temp.parent.ID);
                    /*System.out.println("Node : " + temp.ID
                            + " L: " + (temp.left == null ? "" : temp.left.ID)
                            + " R: " + (temp.right == null ? "" : temp.right.ID)
                            + " P: " + (temp.parent == null ? "" : temp.parent.ID));
*/
                    printNode(temp);
                    //"; L: " + (temp.left == null ? "" : temp.left.ID) + " R: " + (temp.right == null ? "" : temp.right.ID)
                    if (temp.left == null) {
                        bfs.addLast(dummy);
                    } else {
                        once = true;
                        bfs.addLast(temp.left);
                    }

                    if (temp.right == null) {
                        bfs.addLast(dummy);
                    } else {
                        once = true;
                        bfs.addLast(temp.right);
                    }
                }
            }
            c = 2 * c;
            System.out.println("");
            if (!once) break;
        }
    }

    private void printNode(TreeNode temp) {
        System.out.print(temp.ID + "(" + temp.count + " " + (temp.color == RED ? "R" : "B") +
                " P: " + (temp.parent == null ? "" : temp.parent.ID) +
                "; L: " + (temp.left == null ? "" : temp.left.ID) + " R: " + (temp.right == null ? "" : temp.right.ID) +
                "),");
    }

    private void inOrder(TreeNode node) {
        if (node == null) return;
        inOrder(node.left);
        System.out.println(node.ID + " " + node.count);
        inOrder(node.right);
    }
}
