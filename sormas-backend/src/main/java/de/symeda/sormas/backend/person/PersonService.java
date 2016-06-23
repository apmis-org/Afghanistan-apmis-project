package de.symeda.sormas.backend.person;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.backend.common.AbstractAdoService;



@Stateless
@LocalBean
//@RolesAllowed({
//		Permission._ADMIN,
//		Permission._USER })
//@DeclareRoles({
//		Permission._ADMIN,
//		Permission._USER })
public class PersonService extends AbstractAdoService<Person> {

	public PersonService() {
		super(Person.class);
	}

	public Person createPerson() {

		Person person = new Person();
		return person;
	}
}
