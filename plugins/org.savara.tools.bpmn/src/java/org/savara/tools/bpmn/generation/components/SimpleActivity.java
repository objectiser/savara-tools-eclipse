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

/**
 * This class represents a simple task within a
 * BPMN process.
 *
 */
public class SimpleActivity extends AbstractBPMNActivity {

	/**
	 * This constructor initializes the simple activity.
	 * 
	 * @param act The behavioral activity
	 * @param parent The parent BPMN state
	 * @param model The BPMN model
	 */
	public SimpleActivity(org.pi4soa.service.behavior.ActivityType act,
			BPMNActivity parent,
			org.savara.tools.bpmn.generation.BPMNModelFactory model,
			org.savara.tools.bpmn.generation.BPMNNotationFactory notation) {
		super(parent, model, notation);
		
		// Create a state
		/*
		Activity region=getTopLevelActivity();
		
		if (region != null) {
			m_node = (ActivityNode)
				region.createNode(null, UMLPackage.eINSTANCE.getCallBehaviorAction());
			m_node.getInPartitions().add(getActivityPartition());
			
			if (label != null) {
				m_node.setName(label);
			}
		}
		*/
		m_node = model.createSimpleTask(getContainer(), act);
		
	}
	
	/**
	 * Internal constructor which can be used to wrap a vertex.
	 * 
	 * @param vertex The vertex
	 * @param parent The parent state
	 */
	/*
	protected SimpleActivity(org.eclipse.uml2.uml.ActivityNode node, BPMNActivity parent) {
		super(parent);
		
		m_node = node;
	}
	*/
	
	/**
	 * This method sets the association between this state and
	 * the sub-state machine that it represents.
	 * 
	 * @param subMachine The sub state machine
	 */
	/*
	public void setSubStateMachine(ActivityModel subMachine) {
		
		((State)m_node).setSubmachine(subMachine.getStateMachine());
	}
	*/

	/**
	 * This method returns the start node for the activites
	 * represented by this UML activity implementation.
	 * 
	 * @return The starting node
	 */
	public Object getStartNode() {
		return(m_node);
	}
	
	/**
	 * This method returns the end node for the activities
	 * represented by this UML activity implementation.
	 * 
	 * @return The ending node
	 */
	public Object getEndNode() {
		return(m_node);
	}
	
	/**
	 * This method returns the start state.
	 * 
	 * @return The start state
	 */
	public BPMNActivity getStartState() {
		return(this);
	}
	
	/**
	 * This method returns the end state.
	 * 
	 * @return The end state
	 */
	public BPMNActivity getEndState() {
		return(this);
	}
	
	public int getWidth() {
		return(m_width);
	}
	
	public int getHeight() {
		return(60);
	}
	
	public void adjustWidth(int width) {
		m_width = width;
	}
	
	public void draw(Object parent) {
		getNotationFactory().createTask(getModelFactory(),
				m_node, parent, getX(), getY(), getWidth(), getHeight());
	}
	
	private Object m_node=null;
	private int m_width=110;
}
