export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;          // номер текущей страницы (с 0)
  size: number;            // размер страницы
  first: boolean;
  last: boolean;
  numberOfElements: number; // фактическое кол-во на этой странице
  empty: boolean;
}