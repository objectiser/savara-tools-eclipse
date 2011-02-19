/*
 * Copyright 2005-7 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * Jan 25, 2007 : Initial version created by gary
 */
package org.savara.tools.bpmn.generation.components;

public abstract class AbstractBPMNActivity implements BPMNActivity {

	/**
	 * This is the constructor for the default BPMN activity.
	 * 
	 * @param parent The parent
	 */
	public AbstractBPMNActivity(BPMNActivity parent,
			org.savara.tools.bpmn.generation.BPMNModelFactory model,
			org.savara.tools.bpmn.generation.BPMNNotationFactory notation) {
		m_parent = parent;
		m_modelFactory = model;
		m_notationFactory = notation;
		
		if (parent instanceof AbstractBPMNActivity) {
			((AbstractBPMNActivity)parent).addChildState(this);
		}
	}
	
	/**
	 * This method returns the parent BPMN activity.
	 * 
	 * @return The parent
	 */
	public BPMNActivity getParent() {
		return(m_parent);
	}
	
	/**
	 * This method returns the container associated with the
	 * activity.
	 * 
	 * @return The container
	 */
	public Object getContainer() {
		return(m_parent.getContainer());
	}
	
	/**
	 * This method returns the model.
	 * 
	 * @return The model
	 */
	protected org.savara.tools.bpmn.generation.BPMNModelFactory getModelFactory() {
		return(m_modelFactory);
	}
	
	/**
	 * This method returns the notation factory.
	 * 
	 * @return The notation factory
	 */
	protected org.savara.tools.bpmn.generation.BPMNNotationFactory getNotationFactory() {
		return(m_notationFactory);
	}
	
	/**
	 * This method adds a child BPMN state.
	 * 
	 * @param child The child BPMN state
	 */
	protected void addChildState(BPMNActivity child) {
		m_childStates.add(child);
	}
	
	/**
	 * This method returns the list of child states.
	 * 
	 * @return The child states
	 */
	protected java.util.Vector getChildStates() {
		return(m_childStates);
	}
	
	/**
	 * This method indicates that the BPMN state for the
	 * child nodes is complete.
	 *
	 */
	public void childrenComplete() {
	}
	
	/**
	 * This method returns the containing BPMN diagram.
	 * 
	 * @return The BPMN diagram
	 */
	public BPMNDiagram getBPMNDiagram() {
		BPMNDiagram ret=null;
		
		if (m_parent != null) {
			ret = m_parent.getBPMNDiagram();
		}
		
		return(ret);
	}
	
	/**
	 * This method returns the BPMN Activity Model.
	 * 
	 * @return The activity model
	 */
	/*
	public Activity getTopLevelActivity() {
		Activity ret=null;
		
		if (m_parent != null) {
			ret = m_parent.getTopLevelActivity();
		}
		
		return(ret);
	}
	*/
	
	/**
	 * This method returns the BPMN Activity Partition.
	 * 
	 * @return The activity model
	 */
	/*
	public ActivityPartition getActivityPartition() {
		ActivityPartition ret=null;
		
		if (m_parent != null) {
			ret = m_parent.getActivityPartition();
		}
		
		return(ret);
	}
	*/
	
	/**
	 * This method causes a transition to be established from
	 * the supplied BPMN state to the current state, applying
	 * any relevant information (such as event triggers
	 * or conditios) to the transition as appropriate.
	 * 
	 * @param fromNode The source node
	 * @param expression The optional conditional expression
	 * @return The edge
	 */
	public Object transitionFrom(BPMNActivity fromNode,
					String conditionalExpression) {
		Object ret=getModelFactory().createControlLink(getContainer(),
				fromNode.getEndNode(),
				getStartNode(), conditionalExpression);
		
		/*
		Activity activity=getTopLevelActivity();
		ActivityEdge ret=(ActivityEdge)
				activity.createEdge(null, UMLPackage.eINSTANCE.getControlFlow());
		ret.getInPartitions().add(getActivityPartition());
		
		ret.setSource(fromNode.getEndNode());
		ret.setTarget(getStartNode());
		*/
		
		return(ret);
	}
	
	/**
	 * This method joins a list of sequential child BPMN states with transitions.
	 * 
	 * @param children The list of child BPMN states
	 */
	public void transitionSequentialNodes() {
		int width=0;
		int height=0;
		
		if (getChildStates().size() > 0) {
			BPMNActivity v1=(BPMNActivity)getChildStates().get(0);
			width += v1.getWidth();
			height = v1.getHeight();
		}
		
		for (int i=1; i < getChildStates().size(); i++) {
			BPMNActivity v1=(BPMNActivity)getChildStates().get(i-1);
			BPMNActivity v2=(BPMNActivity)getChildStates().get(i);
			
			width += v2.getWidth();
			
			width += HORIZONTAL_GAP; // Gap
			
			if (height < v2.getHeight()) {
				height = v2.getHeight();
			}

			Object endNode=v1.getEndNode();
			
			if (v1.canDeleteEndNode() &&
					(getModelFactory().isJoin(endNode) || // instanceof org.eclipse.uml2.uml.MergeNode ||
					getModelFactory().isTerminal(endNode))) { // instanceof org.eclipse.uml2.uml.FlowFinalNode) {

				// Move the incoming transitions from the junction
				// to the next state
				java.util.List list=getModelFactory().getInboundControlLinks(endNode);
				for (int j=list.size()-1; j >= 0; j--) {
					Object transition=list.get(j);
					
					getModelFactory().setTarget(transition, v2.getStartNode());
					//transition.setTarget(v2.getStartNode());
				}
				
				// Remove Junction
				getModelFactory().delete(endNode);
				
			} else {
				v2.transitionFrom(v1, null);
			}
		}
		
		//height += VERTICAL_GAP; // Gap
		
		setWidth(width);
		setHeight(height);
//System.out.println("THIS: children="+m_childStates.size()+
//		" this="+this+" width="+width+" height="+height);		
	}
	
	public int getX() {
		return(m_x);
	}
	
	public void setX(int x) {
//System.out.println("SETX: this="+this+" x="+x);		
		m_x = x;
	}

	public int getY() {
		return(m_y);
	}
	
	public void setY(int y) {
//System.out.println("SETY: this="+this+" y="+y);		
		m_y = y;
	}

	public int getWidth() {
		return(m_width);
	}
	
	public void setWidth(int width) {
		m_width = width;
	}

	public int getHeight() {
		return(m_height);
	}
	
	public void setHeight(int height) {
		m_height = height;
	}

	public void adjustWidth(int width) {
		float percentChange=width/getWidth();
		
		//setWidth(width);
		
		for (int i=0; i < getChildStates().size(); i++) {
			BPMNActivity act=(BPMNActivity)getChildStates().get(i);
			
			int cur=act.getWidth();
			int newWidth=(int)((float)cur * percentChange);
			
			act.adjustWidth(newWidth);
			
			int change=act.getWidth()-cur;
			
			setWidth(getWidth()+change);
		}
	}
	
	public void calculatePosition(int x, int y) {
		setX(x);
		setY(y);
	}

	public void draw(Object parent) {
	}
	
	public boolean canDeleteEndNode() {
		return(true);
	}
	
	public static final int VERTICAL_GAP = 40;
	public static final int HORIZONTAL_GAP = 50;
	
	private BPMNActivity m_parent=null;
	private org.savara.tools.bpmn.generation.BPMNModelFactory m_modelFactory=null;
	private org.savara.tools.bpmn.generation.BPMNNotationFactory m_notationFactory=null;
	private java.util.Vector m_childStates=new java.util.Vector();
	private int m_x=0;
	private int m_y=0;
	private int m_width=0;
	private int m_height=0;
}
