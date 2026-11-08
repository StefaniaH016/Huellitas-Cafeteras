import { TestBed } from '@angular/core/testing';
import { StepsComponent } from './steps.component';

it('renders every step and marks the current one', () => {
  const fixture = TestBed.createComponent(StepsComponent);
  fixture.componentRef.setInput('pasos', ['Uno', 'Dos', 'Tres']);
  fixture.componentRef.setInput('actual', 2);
  fixture.detectChanges();
  const el = fixture.nativeElement as HTMLElement;

  expect(el.textContent).toContain('Tres');
  expect(el.querySelector('[aria-current="step"]')?.textContent).toContain('Dos');
});
