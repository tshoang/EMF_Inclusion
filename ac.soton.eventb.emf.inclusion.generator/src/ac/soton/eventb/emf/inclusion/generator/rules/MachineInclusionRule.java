/*******************************************************************************
 * Copyright (c) 2017, 2021 University of Southampton.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     University of Southampton - initial API and implementation
 *******************************************************************************/
package ac.soton.eventb.emf.inclusion.generator.rules;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import ac.soton.emf.translator.TranslationDescriptor;
import ac.soton.emf.translator.configuration.AbstractRule;
import ac.soton.emf.translator.configuration.IRule;
import org.eventb.emf.core.CorePackage;
import ac.soton.eventb.emf.inclusion.generator.Make;
import ac.soton.eventb.emf.inclusion.MachineInclusion;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.Variable;
import org.eventb.emf.core.machine.Invariant;


/**
 * <p>
 * Implementation of {@link AbstractRule} for translating machine inclusion into flattened Event-B machine
 * </p>
 * 
 * @author Dana
 * @version 0.2
 * @see TranslationDescriptor
 * @since 0.1.0
 */
public class MachineInclusionRule extends AbstractRule implements IRule{
	protected static final EReference components = CorePackage.Literals.PROJECT__COMPONENTS;
	
	@Override
	public boolean enabled(final EObject sourceElement) throws Exception  {
		if(sourceElement instanceof MachineInclusion)
			return true;
		
			
		else
			return false;
	}

	@Override
	public List<TranslationDescriptor> fire(EObject sourceElement, List<TranslationDescriptor> translatedElements) throws Exception {
		List<TranslationDescriptor> ret = new ArrayList<TranslationDescriptor>();
	    Machine sourceMachine = (Machine)sourceElement.eContainer(); 
		
		MachineInclusion mch = (MachineInclusion) sourceElement;
		Machine abstractMch = mch.getAbstractMachine();
		if(abstractMch != null && !mch.getPrefixes().isEmpty()){
			for(String pref: mch.getPrefixes()){
				sourceMachine.getVariables().addAll(0,copyVariables(abstractMch, pref));
				sourceMachine.getInvariants().addAll(0,copyInvariants(abstractMch, pref));
			}
		}
		else if(abstractMch != null && mch.getPrefixes().isEmpty()){
			sourceMachine.getVariables().addAll(0,copyVariables(abstractMch, ""));
			sourceMachine.getInvariants().addAll(0,copyInvariants(abstractMch, ""));
		}
			
		return ret;	
	}
	
	@Override
	public boolean dependenciesOK(EObject sourceElement, final List<TranslationDescriptor> translatedElements) throws Exception  {
		return true;
	}
	
	@Override
	public boolean fireLate() {
		return false;
	}
	

	//****************************************************************
    // Private Methods to clone the machine related elements
    //****************************************************************
		

	private List<Variable> copyVariables(Machine mch, String prefix){
		ArrayList <Variable> varList = new ArrayList<Variable>();
		
		for(Variable var : mch.getVariables()){
			Variable newVar;
			if(prefix != "")
				newVar = Make.variable(prefix + "_" + var.getName(), var.getComment());
			else
				newVar = Make.variable(var.getName(), var.getComment());
			varList.add(newVar);
		}		
		return varList;		
	}
	
	private List<Invariant> copyInvariants(Machine mch, String prefix){
		ArrayList <Invariant> invList = new ArrayList<Invariant>();
		
		for(Invariant inv : mch.getInvariants()){
			Invariant newInv;
			if(prefix != ""){
				
				String pred = renameInvariant(mch, inv.getPredicate(), prefix);
				newInv= Make.invariant(prefix + "_" + inv.getName(), inv.isTheorem(),pred,inv.getComment());
			}
				
			else
				newInv = Make.invariant(inv.getName(), inv.isTheorem(), inv.getPredicate(),inv.getComment());
			
			invList.add(newInv);
		}		
		return invList;		
	}
	private String renameInvariant(Machine sourceMachine, String predicate, String prefix) {
		String newPredicate = predicate;
		String[] tokens = predicate.split("\\W");
		//Remove repeated tokens
		ArrayList<String> newTokens = removeReptition(tokens);
	
		for (String tok : newTokens){
			for (Variable v : sourceMachine.getVariables()){
				if (tok.equals(v.getName()))
					newPredicate = newPredicate.replaceAll("\\b" + tok + "\\b", prefix + "_" + tok);				
			}
		}
		return newPredicate;
	}
	
	// If a utilities class is defined move this method there
	private ArrayList<String> removeReptition(String[] tokens){
		ArrayList<String> newTokens = new ArrayList<String>();
		for(String tok: tokens){
			if(!newTokens.contains(tok))
				newTokens.add(tok);
		}
		return newTokens;
	}
}
