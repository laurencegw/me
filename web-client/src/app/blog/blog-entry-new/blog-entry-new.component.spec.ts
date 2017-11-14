import {async, ComponentFixture, TestBed, tick} from '@angular/core/testing';

import {BlogEntryNewComponent} from './blog-entry-new.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {StubBlogService} from "../blog.service";
import {By} from "@angular/platform-browser";
import {RouterTestingModule} from "@angular/router/testing";
import {Component} from "@angular/core";
import {Router} from "@angular/router";


@Component({
  template: ''
})
class StubComponent {
}


describe('BlogEntryNewComponent', () => {
  let component: BlogEntryNewComponent;
  let fixture: ComponentFixture<BlogEntryNewComponent>;
  let blogServiceStub: StubBlogService;
  let router: Router;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BlogEntryNewComponent, StubComponent],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([
          {path: "new", component: BlogEntryNewComponent},
          {path: ":id", component: StubComponent},
        ])
      ],
      providers: [
        {provide: "BlogService", useClass: StubBlogService},
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BlogEntryNewComponent);
    blogServiceStub = TestBed.get("BlogService");
    router = TestBed.get(Router);
    router.navigate(["new"]);
    blogServiceStub.blogEntries = [];
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should create a Blog Entry when create is pressed', () => {
    const title = "My new Blog";
    const content = "Some stuff I want to share";
    const titleElement = fixture.debugElement.query(By.css("#title")).nativeElement;
    titleElement.value = title;
    titleElement.dispatchEvent(new Event('input'));
    const contentElement = fixture.debugElement.query(By.css("#content")).nativeElement;
    contentElement.value = content;
    contentElement.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const createElement = fixture.debugElement.query(By.css("#create-entry-button"));
    createElement.nativeElement.click();
    fixture.detectChanges();

    expect(blogServiceStub.blogEntries.length).toEqual(1);
    expect(blogServiceStub.blogEntries[0].title).toEqual(title);
    expect(blogServiceStub.blogEntries[0].content).toEqual(content);

  });
});
