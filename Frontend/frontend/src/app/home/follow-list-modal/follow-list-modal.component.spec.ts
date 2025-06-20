import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FollowListModalComponent } from './follow-list-modal.component';

describe('FollowListModalComponent', () => {
  let component: FollowListModalComponent;
  let fixture: ComponentFixture<FollowListModalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FollowListModalComponent]
    });
    fixture = TestBed.createComponent(FollowListModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
