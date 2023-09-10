package com.quattage.mechano.foundation.electricity.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.quattage.mechano.foundation.helper.VectorHelper;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.BlockPos;

/***
 * A TransferSystem stores approximate versions of NodeConnections (edges) 
 * and NodeBanks (vertices) objects in an undirected graph represented by 
 * an adjacency matrix. <p>
 * A TransferSystem allows energy to be pushed and pulled from connected nodes
 * in an addressable and optimized way. <p>
 * 
 * * I have been informed that the use of a LinkedList in the Y axis of this matrix actually makes it an adjacency LIST. 
 * * I will continue to refer to this as an adjacency matrix anyway, because it sounds cooler.
 */
public class TransferSystem {
    
    /***
     * Stores the X axis of the adjacency matrix
     */
    private HashMap<SystemVertex, SystemNode> systemMatrix = new HashMap<SystemVertex, SystemNode>();

    /***
     * Instantiates a blank TransferSystem
     */
    public TransferSystem() {}

    /***
     * Populates this TransferSystem with members of a list
     * @param cluster ArrayList of SystemNodes to add to this TransferSystem upon creation
     */
    public TransferSystem(ArrayList<SystemNode> cluster) {
        for(SystemNode node : cluster)
            systemMatrix.put(node.parent, node);
    }

    public boolean addNode(SystemNode node) {
        if(node == null) 
            throw new NullPointerException("Failed to add node to TransferSystem - Cannot store a null node!");
        if(systemMatrix.containsKey(node.parent))
            return false;
        systemMatrix.put(node.parent, node);
        return true;
    }

    /***
     * Removes a node from this network and returns it
     * @param at BlockPos key to remove
     * @return The SystemNode that was removed, if any
     */
    public SystemNode popNode(BlockPos at) {
        return systemMatrix.remove(at);
    }

    /***
     * Removes a node from this network
     * @param at BlockPos key to remove
     * @return True if this TransferSystem was modified as a result of this removal
     */
    public boolean removeNode(BlockPos at) {
        SystemNode removed = systemMatrix.remove(at);
        return removed != null;
    }

    /***
     * Create a link (edge) between the SystemNodes located at the 
     * given indexes. This link is non-directed. It is added 
     * symmetrically to both nodes at both provided indexes.
     * @throws NullPointerException If either provided BlockPos 
     * isn't in this TransferSystem.
     * @param indexFrom Index of one SystemNode to link
     * @param indexTo Index of the other SystemNode to link
     * @return True if the SystemNodes were modified
     */
    public boolean link(BlockPos fP, int fI, BlockPos tP, int tI) {

        SystemVertex from = new SystemVertex(fP, fI);
        SystemVertex to = new SystemVertex(tP, tI);
        requireValidLink("Failed to link SystemNodes", from, to);
        SystemNode nodeF = systemMatrix.get(from);
        SystemNode nodeT = systemMatrix.get(to);
        return nodeF.linkTo(nodeT) && nodeT.linkTo(nodeF);
    }

    /***
     * Create a link (edge) between the SystemNodes located at the 
     * given indexes. This link is non-directed. It is added 
     * symmetrically to both nodes at both provided indexes.
     * @throws NullPointerException If either provided SystemNode
     * does not exist within this TransferSystem.
     * @param first SystemNode to link
     * @param second SystemNode to link
     * @return True if the SystemNodes were modified
     */
    public boolean link(SystemNode first, SystemNode second) {
        requireValidNode("Failed to link SystemNodes", first, second);

        return first.linkTo(second) && second.linkTo(first);
    }

    /***
     * Performs a DFS to determine all of the different "clusters"
     * that form this TransferSystem. Individual vertices that are found to 
     * possess no connections are discarded, and are not included in the 
     * resulting clusters. <strong>Does not modify this system in-place.</strong>
     * 
     * @return ArrayList of TransferSystems formed from the individual clusters 
     * within this TransferSystem.
     */
    public ArrayList<TransferSystem> trySplit() {

        HashSet<SystemVertex> visited = new HashSet<>();
        ArrayList<TransferSystem> clusters = new ArrayList<TransferSystem>();

        for(SystemVertex vertex : systemMatrix.keySet()) {
            if(visited.contains(vertex)) continue;
            ArrayList<SystemNode> clusterContents = new ArrayList<>();
            depthFirstPopulate(vertex, visited, clusterContents);
            if(clusterContents.size() > 1)
                clusters.add(new TransferSystem(clusterContents));
        }
        return clusters;
    }

    /***
     * Populates the given cluster ArrayList with all SystemNodes directly and 
     * indirectly connected to the node at the given BlockPos. The TransferSystem 
     * is traversed recursively, so calls must instantiate their own HashSet and 
     * ArrayList for storage.
     * @param vertex Vertex to begin the saerch outwards from
     * @param visted HashSet (usually just instantiated directly and empty when called) to store visited vertices
     * @param cluster ArrayList which will be populated with all nodes that can be found connected to the given vertex.
     */
    public void depthFirstPopulate(SystemVertex vertex, HashSet<SystemVertex> visited, ArrayList<SystemNode> cluster) {
        SystemNode thisIteration = getNode(vertex);
        visited.add(vertex);
        cluster.add(thisIteration);

        for(SystemVertex neighbor : thisIteration.linkedVertices) {
            if(!visited.contains(neighbor))
                depthFirstPopulate(neighbor, visited, cluster);
        }
    }

    /***
     * Appends all elements from the supplied TransferSystem to this
     * TransferSystem, modifying it in-place.
     * @return This TransferSystem (for chaining)
     */
    public TransferSystem mergeWith(TransferSystem other) {
        systemMatrix.putAll(other.systemMatrix);
        return this;
    }

    /***
     * Retrieves a node in this network based on SystemLink.
     * @param BlockPos
     * @return SystemNode at the given BlockPos
     */
    public SystemNode getNode(SystemVertex pos) {
        return systemMatrix.get(pos);
    }

    /***
     * @return True if this TransferSystem contains the given SystemNode
     */
    public boolean containsNode(SystemNode node) {
        return systemMatrix.containsValue(node);
    }

    /***
     * Gets this TransferSystem as a raw Collection. <p>
     * <strong>Modifying this map is not reccomended.</strong>
     * @return Collection containing all SystemNodes in this TransferSystem
     */
    public Collection<SystemNode> all() {
        return systemMatrix.values();
    }

    /***
     * @return True if this TransferSystem doesn't contain any SystemNodes.
     */
    public boolean isEmpty() {
        return systemMatrix.isEmpty();
    }

    /***
     * @return False if ALL of the SystemNodes in this TranferSystem are empty
     * (This network has no edges)
     */
    public boolean hasLinks() {
        for(SystemNode node : systemMatrix.values())
            if(!node.isEmpty()) return true;
        return false;
    }

    /***
     * Genreates a Color for this TransferSystem. Useful for debugging.
     * @return a Color representing this TransferSystem
     */
    public Color getDebugColor() {
        return VectorHelper.toColor(((SystemNode)systemMatrix.values().toArray()[0]).getPos().getCenter());
    }

    public int size() {
        return systemMatrix.size();
    }

    public String toString() {
        String output = "";
        int x = 1;
        for(SystemNode node : systemMatrix.values()) {
            output += "\tNode " + x + ": " + node + "\n";
            x++;
        }
        return output;
    }

    public void requireValidLink(String failMessage, SystemVertex... linkSet) {
        for(SystemVertex link : linkSet) {
            if(link == null) 
                throw new NullPointerException(failMessage + " - The provided SystemLink is null!");
            if(!systemMatrix.containsKey(link.getPos()))
                throw new NullPointerException(failMessage + " - No valid SystemNode matching SystemLink " 
                + link + " could be found!");
        }
    }

    public void requireValidNode(String failMessage, SystemNode... nodeSet) {
        for(SystemNode node : nodeSet) {
            if(node == null)
                throw new NullPointerException(failMessage + " - The provided SystemNode is null!");
            if(!systemMatrix.containsValue(node))
                throw new NullPointerException(failMessage + " - The provided SystemNode does not exist in this TransferSystem!");
        }       
    }
}