package com.v1.pension.model.audit;

import com.v1.pension.utils.CurrentUser;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {
  @Override
  public void newRevision(Object o) {
    var customRevisionEntity = (CustomRevisionEntity) o;
    var username = CurrentUser.getUserName();

    if (!username.isBlank()) {
      customRevisionEntity.setUsername(username);
    } else {
      customRevisionEntity.setUsername("anonymous");
    }
  }
}
