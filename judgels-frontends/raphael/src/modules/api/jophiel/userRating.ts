export interface UserRating {
  publicRating: number;
  hiddenRating: number;
}

export function getRatingName(rating?: UserRating) {
  if (rating === null || rating === undefined) {
    return 'unrated';
  }
  const publicRating = rating.publicRating;
  if (publicRating < 1650) {
    return 'gray';
  }
  if (publicRating < 1750) {
    return 'green';
  }
  if (publicRating < 2000) {
    return 'blue';
  }
  if (publicRating < 2200) {
    return 'purple';
  }
  if (publicRating < 2500) {
    return 'orange';
  }
  if (publicRating < 3000) {
    return 'red';
  }
  return 'legend';
}

export function getRatingClass(rating?: UserRating) {
  return 'rating-' + getRatingName(rating);
}
